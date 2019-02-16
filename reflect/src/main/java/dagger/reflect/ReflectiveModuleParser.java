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

import static dagger.reflect.DaggerReflect.notImplemented;
import static dagger.reflect.Reflection.findMapKey;
import static dagger.reflect.Reflection.findQualifier;
import static dagger.reflect.Reflection.findScope;
import static java.lang.reflect.Modifier.ABSTRACT;
import static java.lang.reflect.Modifier.PRIVATE;
import static java.lang.reflect.Modifier.STATIC;

final class ReflectiveModuleParser {
  static void parse(Class<?> moduleClass, @Nullable Object instance,
      BindingMap.Builder bindingsBuilder) {
    for (Class<?> target : Reflection.getDistinctTypeHierarchy(moduleClass)) {
      for (Method method : target.getDeclaredMethods()) {
        if ((method.getModifiers() & PRIVATE) != 0) {
          throw new IllegalArgumentException("Private module methods are not allowed: " + method);
        }

        Type returnType = method.getGenericReturnType();
        Annotation[] annotations = method.getAnnotations();
        Annotation qualifier = findQualifier(annotations);

        Key key;
        Binding binding;
        if ((method.getModifiers() & ABSTRACT) != 0) {
          if (method.getAnnotation(Binds.class) != null) {
            key = Key.of(qualifier, returnType);
            binding = new UnlinkedBindsBinding(method);
          } else if (method.getAnnotation(BindsOptionalOf.class) != null) {
            key = Key.of(qualifier, new ParameterizedTypeImpl(null, Optional.class, returnType));
            binding = new UnlinkedOptionalBinding(method);
          } else {
            continue;
          }
        } else {
          if ((method.getModifiers() & STATIC) == 0 && instance == null) {
            throw new IllegalStateException(moduleClass.getCanonicalName() + " must be set");
          }

          if (method.getAnnotation(Provides.class) != null) {
            key = Key.of(qualifier, returnType);
            binding = new UnlinkedProvidesBinding(instance, method);
          } else {
            continue;
          }
        }

        Annotation scope = findScope(annotations);
        if (scope != null) {
          // TODO check correct scope.
          throw notImplemented("Scoped bindings");
        }

        if (method.getAnnotation(IntoSet.class) != null) {
          addSetBinding(bindingsBuilder, key, binding);
        } else if (method.getAnnotation(ElementsIntoSet.class) != null) {
          addSetElementsBinding(bindingsBuilder, key, binding);
        } else if (method.getAnnotation(IntoMap.class) != null) {
          addMapBinding(bindingsBuilder, key, binding, annotations);
        } else {
          bindingsBuilder.add(key, binding);
        }
      }
    }
  }

  private static void addSetBinding(BindingMap.Builder bindingsBuilder, Key elementKey,
      Binding elementBinding) {
    Key key = Key.of(elementKey.qualifier(),
        new ParameterizedTypeImpl(null, Set.class, elementKey.type()));
    bindingsBuilder.addIntoSet(key, elementBinding);
  }

  private static void addSetElementsBinding(BindingMap.Builder bindingsBuilder, Key setKey,
      Binding elementsBinding) {
    if (Types.getRawType(setKey.type()) != Set.class) {
      throw new IllegalStateException(); // TODO must be set
    }
    bindingsBuilder.addElementsIntoSet(setKey, elementsBinding);
  }

  private static void addMapBinding(BindingMap.Builder bindingsBuilder, Key entryValueKey,
      Binding entryValueBinding, Annotation[] annotations) {
    Annotation entryKeyAnnotation = findMapKey(annotations);
    if (entryKeyAnnotation == null) {
      throw new IllegalStateException(); // TODO map key required. mention runtime retention.
    }
    Class<? extends Annotation> entryKeyAnnotationType = entryKeyAnnotation.annotationType();
    MapKey mapKeyAnnotation = entryKeyAnnotationType.getAnnotation(MapKey.class);

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
    bindingsBuilder.addIntoMap(key, entryKey, entryValueBinding);
  }
}
