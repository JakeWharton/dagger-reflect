package dagger.reflect;

import dagger.Binds;
import dagger.BindsOptionalOf;
import dagger.Provides;
import dagger.multibindings.ElementsIntoSet;
import dagger.multibindings.IntoMap;
import dagger.multibindings.IntoSet;
import dagger.reflect.TypeUtil.ParameterizedTypeImpl;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Optional;
import org.jetbrains.annotations.Nullable;

import static dagger.reflect.DaggerReflect.notImplemented;
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
          bindingsBuilder.addIntoSet(key, binding);
        } else if (method.getAnnotation(ElementsIntoSet.class) != null) {
          throw notImplemented("@ElementsIntoSet");
        } else if (method.getAnnotation(IntoMap.class) != null) {
          throw notImplemented("@IntoMap");
        } else {
          bindingsBuilder.add(key, binding);
        }
      }
    }
  }
}
