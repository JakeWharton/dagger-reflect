package dagger.reflect;

import static dagger.reflect.Reflection.findQualifier;

import dagger.MembersInjector;
import dagger.reflect.Binding.UnlinkedBinding;
import dagger.reflect.ReflectiveAssistedFactoryLookupFactory.AssistedParameter;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.Map;

class UnlinkedAssistedFactoryBinding<T> extends UnlinkedBinding {
  private final Class<T> factory;
  private final Class<?> assistedType;
  private final Constructor<?> assistedConstructor;
  private final Map<Integer, AssistedParameter> factoryParameters;
  private final Map<Integer, AssistedParameter> assistedInjectParameters;
  private final ParameterTypesResolver<?> assistedConstructorParameterTypesResolver;

  UnlinkedAssistedFactoryBinding(
      Class<T> factory,
      Class<?> assistedType,
      Constructor<?> constructor,
      Map<Integer, AssistedParameter> factoryParameters,
      Map<Integer, AssistedParameter> injectParameters,
      ParameterTypesResolver<?> assistedConstructorParameterTypesResolver) {
    this.factory = factory;
    this.assistedType = assistedType;
    this.assistedConstructor = constructor;
    this.factoryParameters = factoryParameters;
    this.assistedInjectParameters = injectParameters;
    this.assistedConstructorParameterTypesResolver = assistedConstructorParameterTypesResolver;
  }

  @Override
  public LinkedBinding<T> link(Linker linker, Scope scope) {
    Type[] parameterTypes = assistedConstructorParameterTypesResolver.getActualParameterTypes();
    Annotation[][] parameterAnnotations = assistedConstructor.getParameterAnnotations();

    Integer[] constructorParameterToFactoryParameterIndex = new Integer[parameterTypes.length];
    LinkedBinding<?>[] bindings = new LinkedBinding<?>[parameterTypes.length];

    // TODO find a better way to resolve binding for the factory itself
    // If we get the binding from linker it leads to dependency cycle
    // Needed to inject factory instance into an assistedType (why would you do that though?)
    // and pass dagger tests
    Integer factoryBinding = null;

    for (int i = 0; i < parameterTypes.length; i++) {
      AssistedParameter assistedParameter = assistedInjectParameters.get(i);

      if (assistedParameter != null) {
        for (Map.Entry<Integer, AssistedParameter> entry : factoryParameters.entrySet()) {
          // One of parameters must match, because we validate assisted parameters earlier
          if (entry.getValue().equals(assistedParameter)) {
            constructorParameterToFactoryParameterIndex[i] = entry.getKey();
            break;
          }
        }
      } else {
        Type parameterType = parameterTypes[i];
        Key key = Key.of(findQualifier(parameterAnnotations[i]), parameterType);

        if (Types.equals(parameterType, factory)) {
          factoryBinding = i;
        } else {
          bindings[i] = linker.get(key);
        }
      }
    }

    MembersInjector<?> membersInjector = ReflectiveMembersInjector.create(assistedType, scope);

    LinkedBinding<T> linkedBinding =
        new LinkedJustInTimeAssistedFactoryBinding<>(
            factory,
            assistedConstructor,
            membersInjector,
            bindings,
            constructorParameterToFactoryParameterIndex);

    if (factoryBinding != null) {
      bindings[factoryBinding] = linkedBinding;
    }

    return linkedBinding;
  }

  @Override
  public String toString() {
    return "@AssistedInject["
        + assistedType.getName()
        + assistedConstructorParameterTypesResolver.getTypeArgumentsStringOrEmpty()
        + ".<init>(…)]";
  }
}
