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

  @Override public LinkRequest request() {
    Type[] parameterTypes = method.getGenericParameterTypes();
    if (parameterTypes.length == 0) {
      return LinkRequest.EMPTY;
    }
    Annotation[][] parameterAnnotations = method.getParameterAnnotations();
    Key[] dependencies = new Key[parameterTypes.length];
    for (int i = 0; i < parameterTypes.length; i++) {
      dependencies[i] = Key.of(findQualifier(parameterAnnotations[i]), parameterTypes[i]);
    }
    return new LinkRequest(dependencies);
  }

  @Override public LinkedBinding<?> link(LinkedBinding<?>[] dependencies) {
    return new LinkedProvidesBinding<>(instance, method, dependencies);
  }
}
