package dagger.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import org.jetbrains.annotations.Nullable;

import static dagger.reflect.Reflection.findQualifier;

final class UnlinkedProvidesBinding extends Binding.UnlinkedBinding {
  private final @Nullable Object instance;
  private final Method method;

  UnlinkedProvidesBinding(@Nullable Object instance, Method method) {
    this.instance = instance;
    this.method = method;
  }

  @Override public LinkedBinding<?> link(Linker linker, Scope scope) {
    Type[] parameterTypes = method.getGenericParameterTypes();
    Annotation[][] parameterAnnotations = method.getParameterAnnotations();
    LinkedBinding<?>[] dependencies = new LinkedBinding<?>[parameterTypes.length];
    for (int i = 0; i < parameterTypes.length; i++) {
      Key key = Key.of(findQualifier(parameterAnnotations[i]), parameterTypes[i]);
      dependencies[i] = linker.get(key);
    }
    return new LinkedProvidesBinding<>(instance, method, dependencies);
  }

  @Override public String toString() {
    return "@Provides[" + method.getDeclaringClass().getName() + '.' + method.getName() + "(…)]";
  }
}
