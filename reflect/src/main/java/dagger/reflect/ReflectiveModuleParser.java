package dagger.reflect;

import dagger.Binds;
import dagger.BindsOptionalOf;
import dagger.MapKey;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import dagger.multibindings.IntoMap;
import dagger.multibindings.IntoSet;
import dagger.reflect.TypeUtil.ParameterizedTypeImpl;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

import static dagger.reflect.Reflection.findAnnotation;
import static dagger.reflect.Reflection.findMapKey;
import static dagger.reflect.Reflection.findQualifier;
import static dagger.reflect.Reflection.findScope;
import static dagger.reflect.Reflection.maybeInstantiate;
import static dagger.reflect.Reflection.requireAnnotation;
import static java.lang.reflect.Modifier.ABSTRACT;
import static java.lang.reflect.Modifier.PRIVATE;
import static java.lang.reflect.Modifier.STATIC;

final class ReflectiveModuleParser {
  static void parse(Class<?> moduleClass, @Nullable Object instance, Scope.Builder scopeBuilder) {
    for (Class<?> target : Reflection.getDistinctTypeHierarchy(moduleClass)) {
      for (Method method : target.getDeclaredMethods()) {
        Type returnType = method.getGenericReturnType();
        Annotation[] annotations = method.getAnnotations();
        Annotation qualifier = findQualifier(annotations);

        if ((method.getModifiers() & ABSTRACT) != 0) {
          if (method.getAnnotation(Binds.class) != null) {
            Key key = Key.of(qualifier, returnType);
            Binding binding = new UnlinkedBindsBinding(method);
            addBinding(scopeBuilder, key, binding, annotations);
          } else if (method.getAnnotation(BindsOptionalOf.class) != null) {
            try {
              Key key =
                  Key.of(qualifier, new ParameterizedTypeImpl(null, Optional.class, returnType));
              Binding binding = new UnlinkedJavaOptionalBinding(method);
              addBinding(scopeBuilder, key, binding, annotations);
            } catch (NoClassDefFoundError ignored) {
            }
            try {
              Key key = Key.of(qualifier,
                  new ParameterizedTypeImpl(null, com.google.common.base.Optional.class,
                      returnType));
              Binding binding = new UnlinkedGuavaOptionalBinding(method);
              addBinding(scopeBuilder, key, binding, annotations);
            } catch (NoClassDefFoundError ignored) {
            }
          }
        } else {
          if ((method.getModifiers() & STATIC) == 0 && instance == null) {
            // Try to just-in-time create an instance of the module using a default constructor.
            instance = maybeInstantiate(moduleClass);
            if (instance == null) {
              throw new IllegalStateException(moduleClass.getCanonicalName() + " must be set");
            }
          }

          if (method.getAnnotation(Provides.class) != null) {
            ensureNotPrivate(method);
            Key key = Key.of(qualifier, returnType);
            Binding binding = new UnlinkedProvidesBinding(instance, method);
            addBinding(scopeBuilder, key, binding, annotations);
          }
        }
      }
    }
  }

  private static void addBinding(Scope.Builder scopeBuilder, Key key, Binding binding,
      Annotation[] annotations) {
    Annotation scope = findScope(annotations);
    if (scope != null) {
      if (!scopeBuilder.annotations.contains(scope)) {
        throw new IllegalStateException(); // TODO wrong scope
      }
      binding = binding.asScoped();
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

  private static void addSetBinding(Scope.Builder scopeBuilder, Key elementKey,
      Binding elementBinding) {
    Key key = Key.of(elementKey.qualifier(),
        new ParameterizedTypeImpl(null, Set.class, elementKey.type()));
    scopeBuilder.addBindingIntoSet(key, elementBinding);
  }

  private static void addSetElementsBinding(Scope.Builder scopeBuilder, Key setKey,
      Binding elementsBinding) {
    if (Types.getRawType(setKey.type()) != Set.class) {
      throw new IllegalStateException(); // TODO must be set
    }
    scopeBuilder.addBindingElementsIntoSet(setKey, elementsBinding);
  }

  private static void addMapBinding(Scope.Builder scopeBuilder, Key entryValueKey,
      Binding entryValueBinding, Annotation[] annotations) {
    Annotation entryKeyAnnotation = findMapKey(annotations);
    if (entryKeyAnnotation == null) {
      throw new IllegalStateException(); // TODO map key required. mention runtime retention.
    }
    Class<? extends Annotation> entryKeyAnnotationType = entryKeyAnnotation.annotationType();
    MapKey mapKeyAnnotation = requireAnnotation(entryKeyAnnotationType, MapKey.class);

    Class<?> entryKeyType;
    Object entryKey;
    if (mapKeyAnnotation.unwrapValue()) {
      // Find the single declared method on the map key and gets its type and value.
      Method[] methods = entryKeyAnnotationType.getDeclaredMethods();
      if (methods.length != 1) {
        throw new IllegalStateException(); // TODO key annotations can only have a single method
      }
      Method method = methods[0];

      entryKeyType = method.getReturnType();
      entryKey = Reflection.tryInvoke(entryKeyAnnotation, method);
      if (entryKey == null) {
        throw new AssertionError(); // Not allowed by the Java language specification.
      }
    } else {
      entryKeyType = entryKeyAnnotationType;
      entryKey = entryKeyAnnotation;
    }

    Key key = Key.of(entryValueKey.qualifier(),
        new ParameterizedTypeImpl(null, Map.class, entryKeyType, entryValueKey.type()));
    scopeBuilder.addBindingIntoMap(key, entryKey, entryValueBinding);
  }

  private static void ensureNotPrivate(Method method) {
    if ((method.getModifiers() & PRIVATE) != 0) {
      throw new IllegalArgumentException("Provides methods may not be private: " + method);
    }
  }
}
