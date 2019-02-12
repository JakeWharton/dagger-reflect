package dagger.reflect;

import dagger.reflect.Binding.LinkedBinding;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.Set;

import static dagger.reflect.Reflection.findQualifier;

final class ReflectiveComponentParser {
  private static final LinkedBinding<?>[] NO_BINDINGS = new LinkedBinding<?>[0];

  static void parse(Class<?> moduleClass, Object instance,
      BindingGraph.Builder graphBuilder) {
    Set<Class<?>> seen = new LinkedHashSet<>();
    Deque<Class<?>> queue = new ArrayDeque<>();
    queue.add(moduleClass);
    while (!queue.isEmpty()) {
      Class<?> target = queue.removeFirst();
      if (!seen.add(target)) {
        continue; // Duplicate type in hierarchy.
      }

      for (Method method : target.getDeclaredMethods()) {
        if (method.getParameterCount() != 0 || method.getReturnType() == void.class) {
          continue; // Not a provision method.
        }

        Annotation qualifier = findQualifier(method.getAnnotations());
        Type type = method.getGenericReturnType();
        Key key = Key.of(qualifier, type);

        Binding binding = new LinkedProvidesBinding<>(instance, method, NO_BINDINGS);

        graphBuilder.add(key, binding);
      }

      Class<?> superclass = target.getSuperclass();
      if (superclass != Object.class && superclass != null) {
        queue.add(superclass);
      }
      Collections.addAll(queue, target.getInterfaces());
    }
  }

  private ReflectiveComponentParser() {
    throw new AssertionError();
  }
}
