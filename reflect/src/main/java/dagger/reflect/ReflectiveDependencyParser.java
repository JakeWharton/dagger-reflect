package dagger.reflect;

import static dagger.reflect.Reflection.findQualifier;
import static dagger.reflect.Reflection.hasNullable;

import dagger.reflect.Binding.LinkedBinding;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

final class ReflectiveDependencyParser {
  private static final LinkedBinding<?>[] NO_BINDINGS = new LinkedBinding<?>[0];

  static void parse(Class<?> cls, Object instance, Scope.Builder scopeBuilder) {
    for (Class<?> target : Reflection.getDistinctTypeHierarchy(cls)) {
      for (Method method : target.getDeclaredMethods()) {
        if (method.getParameterTypes().length != 0 || method.getReturnType() == void.class) {
          continue; // Not a provision method.
        }

        Annotation qualifier = findQualifier(method.getAnnotations());
        Type type = method.getGenericReturnType();
        Key key = Key.of(qualifier, type);

        Binding binding =
            new LinkedProvidesBinding<>(
                instance, method, NO_BINDINGS, hasNullable(method.getDeclaredAnnotations()));

        scopeBuilder.addBinding(key, binding);
      }
    }
  }

  private ReflectiveDependencyParser() {
    throw new AssertionError();
  }
}
