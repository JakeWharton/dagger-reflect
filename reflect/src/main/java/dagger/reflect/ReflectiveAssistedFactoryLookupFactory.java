package dagger.reflect;

import static dagger.reflect.Reflection.boxIfNecessary;
import static dagger.reflect.Reflection.findScope;
import static dagger.reflect.TypeUtil.canonicalize;

import com.google.auto.value.AutoValue;
import dagger.assisted.Assisted;
import dagger.assisted.AssistedFactory;
import dagger.assisted.AssistedInject;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.inject.Qualifier;
import org.jetbrains.annotations.Nullable;

final class ReflectiveAssistedFactoryLookupFactory implements JustInTimeLookup.Factory {
  @Override
  public @Nullable JustInTimeLookup create(Key key) {
    if (key.qualifier() != null) {
      return null; // AssistedFactory type cannot be qualified
    }

    Type type = key.type();

    try {
      return getJustInTimeAssistedFactoryLookup(type);
    } catch (IllegalStateException t) {
      throw new IllegalStateException(
          String.format("Failed to create @AssistedFactory %s: \n%s", type, t.getMessage()), t);
    }
  }

  private @Nullable <T> JustInTimeLookup getJustInTimeAssistedFactoryLookup(Type type) {
    Class<T> cls = Types.getRawClassOrInterface(type);

    if (cls == null) {
      return null; // Array types can't be an AssistedFactory
    }

    if (!Reflection.hasAnnotation(cls.getAnnotations(), AssistedFactory.class)) {
      return null;
    }

    @SuppressWarnings("UnnecessaryLocalVariable")
    Class<T> factory = cls;
    Method factoryMethod;
    Class<?> assistedType;
    Constructor<?> assistedConstructor;

    // dagger.internal.codegen.AssistedFactoryProcessingStep
    if (!Modifier.isInterface(
        factory.getModifiers()) /* && !Modifier.isAbstract(factory.getModifiers())*/) {
      throw new IllegalStateException(
          // "The @AssistedFactory-annotated type must be either an abstract class or interface"
          // Only interface factories are allowed because proxies
          "The @AssistedFactory-annotated type must be an interface when used with dagger-reflect");
    }

    if (factory.isMemberClass() && !Modifier.isStatic(factory.getModifiers())) {
      throw new IllegalStateException("Nested @AssistedFactory-annotated types must be static. ");
    }

    Set<Method> abstractFactoryMethods = new HashSet<>();
    for (Method method : factory.getMethods()) {
      if (Modifier.isAbstract(method.getModifiers()) && !Reflection.isDefault(method)) {
        abstractFactoryMethods.add(method);
      }
    }

    if (abstractFactoryMethods.isEmpty()) {
      throw new IllegalStateException(
          "The @AssistedFactory-annotated type is missing an abstract, non-default method "
              + "whose return type matches the assisted injection type.");
    }

    if (abstractFactoryMethods.size() > 1) {
      throw new IllegalStateException(
          "The @AssistedFactory-annotated type should contain a single abstract, non-default"
              + " method but found multiple: "
              + abstractFactoryMethods);
    }

    factoryMethod = abstractFactoryMethods.iterator().next();
    assistedType = factoryMethod.getReturnType();
    assistedConstructor = findSingleAssistedInjectConstructor(assistedType);

    if (assistedConstructor == null) {
      throw new IllegalStateException(
          String.format(
              "Invalid return type: %s. An assisted factory's abstract method must return a "
                  + "type with an @AssistedInject-annotated constructor.",
              assistedType));
    }

    if (factoryMethod.getTypeParameters().length != 0) {
      throw new IllegalStateException(
          "@AssistedFactory does not currently support type parameters in the creator "
              + "method. See https://github.com/google/dagger/issues/2279");
    }

    Map<Integer, AssistedParameter> factoryParameters =
        findAssistedParameters(factory, factoryMethod);
    Map<Integer, AssistedParameter> injectParameters =
        findAssistedParameters(assistedType, assistedConstructor);

    Set<AssistedParameter> uniqueAssistedParameters = new HashSet<>();
    for (AssistedParameter assistedParameter : factoryParameters.values()) {
      if (!uniqueAssistedParameters.add(assistedParameter)) {
        throw new IllegalStateException(
            "@AssistedFactory method has duplicate @Assisted types: " + assistedParameter);
      }
    }

    if (!new HashSet<>(injectParameters.values())
        .equals(new HashSet<>(factoryParameters.values()))) {

      StringBuilder parameters = new StringBuilder();
      for (AssistedParameter value : injectParameters.values()) {
        parameters.append(value.type());
        parameters.append(", ");
      }

      throw new IllegalStateException(
          String.format(
              "The parameters in the factory method must match the @Assisted parameters in %s."
                  + "\n      Actual: %s#%s"
                  + "\n    Expected: %s#%s(%s)",
              assistedType.getCanonicalName(),
              factory.getCanonicalName(),
              factoryMethod.getName(),
              factory.getCanonicalName(),
              factoryMethod.getName(),
              parameters.toString()));
    }

    // dagger.internal.codegen.AssistedInjectProcessingStep
    Set<AssistedParameter> uniqueAssistedInjectParameters = new HashSet<>();
    for (AssistedParameter assistedParameter : injectParameters.values()) {
      if (!uniqueAssistedInjectParameters.add(assistedParameter)) {
        throw new IllegalStateException(
            String.format(
                "@AssistedInject constructor has duplicate @Assisted type: %s. "
                    + "Consider setting an identifier on the parameter by using "
                    + "@Assisted(\"identifier\") in both the factory and @AssistedInject constructor",
                assistedParameter));
      }
    }

    // dagger.internal.codegen.AssistedProcessingStep
    for (Annotation[] parameterAnnotations : assistedConstructor.getParameterAnnotations()) {
      if (Reflection.hasAnnotation(parameterAnnotations, Assisted.class)) {
        for (Annotation annotation : parameterAnnotations) {
          for (Annotation annotationOfAnnotation : annotation.annotationType().getAnnotations()) {
            if (annotationOfAnnotation.annotationType() == Qualifier.class) {
              throw new IllegalStateException(
                  "Qualifiers cannot be used with @Assisted parameters");
            }
          }
        }
      }
    }

    Annotation scope = findScope(cls.getAnnotations());
    ParameterTypesResolver<?> parameterTypesResolver =
        ParameterTypesResolver.ofConstructor(assistedType, assistedConstructor);
    Binding binding =
        new UnlinkedAssistedFactoryBinding<>(
            factory,
            assistedType,
            assistedConstructor,
            factoryParameters,
            injectParameters,
            parameterTypesResolver);

    return new JustInTimeLookup(scope, binding);
  }

  @AutoValue
  abstract static class AssistedParameter {

    public static AssistedParameter of(Type type, String qualifier) {
      return new AutoValue_ReflectiveAssistedFactoryLookupFactory_AssistedParameter(
          canonicalize(boxIfNecessary(type)), qualifier);
    }

    abstract Type type();

    abstract String qualifier();

    @Override
    public final String toString() {
      return qualifier().isEmpty()
          ? String.format("@Assisted %s", type())
          : String.format("@Assisted(\"%s\") %s", qualifier(), type());
    }
  }

  private static Map<Integer, AssistedParameter> findAssistedParameters(
      Type type, Object methodOrConstructor) {

    boolean allParametersAreAssisted;
    Annotation[][] executableParameterAnnotations;
    Type[] actualParameterTypes;

    if (methodOrConstructor instanceof Method) {
      allParametersAreAssisted = true;
      executableParameterAnnotations = ((Method) methodOrConstructor).getParameterAnnotations();
      actualParameterTypes =
          ParameterTypesResolver.ofMethod(type, (Method) methodOrConstructor)
              .getActualParameterTypes();
    } else if (methodOrConstructor instanceof Constructor<?>) {
      allParametersAreAssisted = false;
      executableParameterAnnotations =
          ((Constructor<?>) methodOrConstructor).getParameterAnnotations();
      actualParameterTypes =
          ParameterTypesResolver.ofConstructor(type, (Constructor<?>) methodOrConstructor)
              .getActualParameterTypes();
    } else {
      throw new RuntimeException(
          "Expected method or constructor, got " + methodOrConstructor.getClass());
    }

    Map<Integer, AssistedParameter> result = new HashMap<>();

    for (int i = 0; i < executableParameterAnnotations.length; i++) {
      Annotation[] parameterAnnotations = executableParameterAnnotations[i];
      Type parameterType = actualParameterTypes[i];
      Assisted assisted = Reflection.findAnnotation(parameterAnnotations, Assisted.class);

      if (assisted != null || allParametersAreAssisted) {
        String qualifier = assisted == null ? "" : assisted.value();
        result.put(i, AssistedParameter.of(parameterType, qualifier));
      }
    }

    return result;
  }

  private static <T> @Nullable Constructor<T> findSingleAssistedInjectConstructor(Class<T> cls) {
    // Not modifying it, safe to use generics; see Class#getConstructors() for more info.
    @SuppressWarnings("unchecked")
    Constructor<T>[] constructors = (Constructor<T>[]) cls.getDeclaredConstructors();
    Constructor<T> target = null;
    for (Constructor<T> constructor : constructors) {
      if (constructor.getAnnotation(AssistedInject.class) != null) {
        if (target != null) {
          throw new IllegalStateException(
              cls.getCanonicalName()
                  + " defines multiple @AssistedInject-annotations constructors");
        }
        target = constructor;
      }
    }
    return target;
  }
}
