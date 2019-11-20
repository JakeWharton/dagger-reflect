package dagger.reflect;

import static dagger.reflect.Reflection.boxIfNecessary;
import static dagger.reflect.Reflection.findAnnotation;
import static dagger.reflect.Reflection.findMapKey;
import static dagger.reflect.Reflection.findQualifier;
import static dagger.reflect.Reflection.findScope;
import static dagger.reflect.Reflection.findScopes;
import static dagger.reflect.Reflection.maybeInstantiate;
import static dagger.reflect.Reflection.requireAnnotation;

import dagger.Binds;
import dagger.BindsOptionalOf;
import dagger.MapKey;
import dagger.Provides;
import dagger.android.AndroidInjector;
import dagger.android.ContributesAndroidInjector;
import dagger.multibindings.ElementsIntoSet;
import dagger.multibindings.IntoMap;
import dagger.multibindings.IntoSet;
import dagger.multibindings.Multibinds;
import dagger.reflect.TypeUtil.ParameterizedTypeImpl;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

final class ReflectiveModuleParser {
  static void parse(Class<?> moduleClass, @Nullable Object instance, Scope.Builder scopeBuilder) {
    for (Class<?> target : Reflection.getDistinctTypeHierarchy(moduleClass)) {
      for (Method method : target.getDeclaredMethods()) {
        Type returnType = method.getGenericReturnType();
        Annotation[] annotations = method.getAnnotations();
        Annotation qualifier = findQualifier(annotations);

        if (Modifier.isAbstract(method.getModifiers())) {
          if (method.getAnnotation(Binds.class) != null) {
            Key key = Key.of(qualifier, returnType);
            Binding binding = new UnlinkedBindsBinding(method);
            addBinding(scopeBuilder, key, binding, annotations);
          } else if (method.getAnnotation(BindsOptionalOf.class) != null) {
            try {
              Key key =
                  Key.of(
                      qualifier,
                      new ParameterizedTypeImpl(null, Optional.class, boxIfNecessary(returnType)));
              Binding binding = new UnlinkedJavaOptionalBinding(method);
              addBinding(scopeBuilder, key, binding, annotations);
            } catch (NoClassDefFoundError ignored) {
            }
            try {
              Key key =
                  Key.of(
                      qualifier,
                      new ParameterizedTypeImpl(
                          null, com.google.common.base.Optional.class, boxIfNecessary(returnType)));
              Binding binding = new UnlinkedGuavaOptionalBinding(method);
              addBinding(scopeBuilder, key, binding, annotations);
            } catch (NoClassDefFoundError ignored) {
            }
          } else if (method.getAnnotation(Multibinds.class) != null) {
            Key key = Key.of(qualifier, returnType);
            if (method.getReturnType() == Set.class) {
              scopeBuilder.createSetBinding(key);
            } else if (method.getReturnType() == Map.class) {
              scopeBuilder.createMapBinding(key);
            } else {
              throw new IllegalStateException(
                  "@Multibinds return type must be Set or Map: " + returnType);
            }
          } else {
            ContributesAndroidInjector contributesAndroidInjector =
                method.getAnnotation(ContributesAndroidInjector.class);
            if (contributesAndroidInjector != null) {
              // TODO check return type is a supported type? not parameterized? something else?
              Class<?>[] modules = contributesAndroidInjector.modules();
              Class<?> androidType = (Class<?>) returnType;
              Binding.UnlinkedBinding binding =
                  new UnlinkedAndroidInjectorFactoryBinding(
                      modules, androidType, findScopes(annotations));
              addAndroidMapBinding(scopeBuilder, returnType, binding);
            }
          }
        } else {
          if (method.getAnnotation(Provides.class) != null) {
            ensureNotPrivate(method);
            if (!Modifier.isStatic(method.getModifiers()) && instance == null) {
              ensureNotAbstract(moduleClass);
              // Try to just-in-time create an instance of the module using a default constructor.
              instance = maybeInstantiate(moduleClass);
              if (instance == null) {
                throw new IllegalStateException(moduleClass.getCanonicalName() + " must be set");
              }
            }

            Key key = Key.of(qualifier, returnType);
            Binding binding = new UnlinkedProvidesBinding(instance, method);
            addBinding(scopeBuilder, key, binding, annotations);
          }
        }
      }
    }
  }

  private static void addBinding(
      Scope.Builder scopeBuilder, Key key, Binding binding, Annotation[] annotations) {
    Annotation scope = findScope(annotations);
    if (scope != null) {
      if (!scopeBuilder.annotations.contains(scope)) {
        throw new IllegalStateException(
            "[Dagger/IncompatiblyScopedBindings] "
                // TODO clarify which "(sub)component" failed
                // (method when UnlinkedAndroidInjectorFactoryBinding is being created)
                // ([sub]componentClass in when calling ComponentScopeBuilder is calling create)
                + "(sub)component scoped with "
                + scopeBuilder.annotations
                + " may not reference bindings with different scopes:\n"
                + "@"
                + scope.annotationType().getCanonicalName()
                + " "
                + binding);
      } else {
        binding = binding.asScoped();
      }
    }

    if (findAnnotation(annotations, IntoSet.class) != null) {
      addSetBinding(scopeBuilder, key, binding);
    } else if (findAnnotation(annotations, ElementsIntoSet.class) != null) {
      addSetElementsBinding(scopeBuilder, key, binding);
    } else if (findAnnotation(annotations, IntoMap.class) != null) {
      addMapBinding(scopeBuilder, key, binding, annotations);
    } else {
      scopeBuilder.addBinding(key, binding);
    }
  }

  private static void addSetBinding(
      Scope.Builder scopeBuilder, Key elementKey, Binding elementBinding) {
    Key key =
        Key.of(
            elementKey.qualifier(), new ParameterizedTypeImpl(null, Set.class, elementKey.type()));
    scopeBuilder.addBindingIntoSet(key, elementBinding);
  }

  private static void addSetElementsBinding(
      Scope.Builder scopeBuilder, Key setKey, Binding elementsBinding) {
    if (Types.getRawType(setKey.type()) != Set.class) {
      throw new IllegalArgumentException(
          "@BindsIntoSet must return Set. Found " + setKey.type() + ".");
    }
    if (((ParameterizedType) setKey.type()).getActualTypeArguments()[0] instanceof WildcardType) {
      throw new IllegalArgumentException(
          "@Binds methods must return a primitive, an array, a type variable, or a declared type. Found "
              + setKey.type()
              + ".");
    }
    scopeBuilder.addBindingElementsIntoSet(setKey, elementsBinding);
  }

  private static void addAndroidMapBinding(
      Scope.Builder scopeBuilder, Type returnType, Binding entryValueBinding) {
    TypeUtil.WildcardTypeImpl wildcardType =
        new TypeUtil.WildcardTypeImpl(new Type[] {Object.class}, null);
    Type classType = new ParameterizedTypeImpl(null, Class.class, wildcardType);
    Type injectorFactoryType =
        new ParameterizedTypeImpl(
            AndroidInjector.class, AndroidInjector.Factory.class, wildcardType);

    Key key =
        Key.of(null, new ParameterizedTypeImpl(null, Map.class, classType, injectorFactoryType));
    Key string =
        Key.of(null, new ParameterizedTypeImpl(null, Map.class, String.class, injectorFactoryType));
    scopeBuilder.addBindingIntoMap(key, returnType, entryValueBinding);
    scopeBuilder.addBindingIntoMap(string, returnType, entryValueBinding);
  }

  private static void addMapBinding(
      Scope.Builder scopeBuilder,
      Key entryValueKey,
      Binding entryValueBinding,
      Annotation[] annotations) {
    Annotation entryKeyAnnotation = findMapKey(annotations);
    if (entryKeyAnnotation == null) {
      throw new IllegalStateException(); // TODO map key required. mention runtime retention.
    }
    Class<? extends Annotation> entryKeyAnnotationType = entryKeyAnnotation.annotationType();
    MapKey mapKeyAnnotation = requireAnnotation(entryKeyAnnotationType, MapKey.class);

    Type entryKeyType;
    Object entryKey;
    if (mapKeyAnnotation.unwrapValue()) {
      // Find the single declared method on the map key and gets its type and value.
      Method[] methods = entryKeyAnnotationType.getDeclaredMethods();
      if (methods.length != 1) {
        throw new IllegalStateException(); // TODO key annotations can only have a single method
      }
      Method method = methods[0];

      entryKeyType = boxIfNecessary(method.getGenericReturnType());
      entryKey = Reflection.tryInvoke(entryKeyAnnotation, method);
      if (entryKey == null) {
        throw new AssertionError(); // Not allowed by the Java language specification.
      }
    } else {
      entryKeyType = entryKeyAnnotationType;
      entryKey = entryKeyAnnotation;
    }

    Key key =
        Key.of(
            entryValueKey.qualifier(),
            new ParameterizedTypeImpl(null, Map.class, entryKeyType, entryValueKey.type()));
    scopeBuilder.addBindingIntoMap(key, entryKey, entryValueBinding);
  }

  private static void ensureNotPrivate(Method method) {
    if (Modifier.isPrivate(method.getModifiers())) {
      throw new IllegalArgumentException("Provides methods may not be private: " + method);
    }
  }

  private static void ensureNotAbstract(Class<?> moduleClass) {
    if (Modifier.isAbstract(moduleClass.getModifiers())) {
      throw new IllegalStateException(
          moduleClass.getCanonicalName()
              + " is abstract and has instance @Provides methods."
              + " Consider making the methods static or including a non-abstract subclass of the module instead.");
    }
  }
}
