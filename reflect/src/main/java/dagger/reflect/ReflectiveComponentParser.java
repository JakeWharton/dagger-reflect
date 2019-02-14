package dagger.reflect;

import dagger.reflect.Binding.LinkedBinding;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import static dagger.reflect.Reflection.findQualifier;

final class ReflectiveComponentParser {
  private static final LinkedBinding<?>[] NO_BINDINGS = new LinkedBinding<?>[0];

  static void parse(Class<?> moduleClass, Object instance, BindingMap.Builder bindingsBuilder) {
    for (Class<?> target : Reflection.getDistinctTypeHierarchy(moduleClass)) {
      for (Method method : target.getDeclaredMethods()) {
        if (method.getParameterTypes().length != 0 || method.getReturnType() == void.class) {
          continue; // Not a provision method.
        }

        Annotation qualifier = findQualifier(method.getAnnotations());
        Type type = method.getGenericReturnType();
        Key key = Key.of(qualifier, type);

        Binding binding = new LinkedProvidesBinding<>(instance, method, NO_BINDINGS);

        bindingsBuilder.add(key, binding);
      }
    }
  }

  private ReflectiveComponentParser() {
    throw new AssertionError();
  }
}
