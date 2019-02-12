package dagger.reflect;

import dagger.reflect.Binding.UnlinkedBinding;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import static dagger.reflect.Reflection.findQualifier;

public final class UnlinkedOptionalBinding extends UnlinkedBinding {
  private final Method method;

  UnlinkedOptionalBinding(Method method) {
    this.method = method;
  }

  @Override
  public LinkRequest request() {
    Type[] parameterTypes = method.getGenericParameterTypes();
    if (parameterTypes.length != 0) {
      throw new IllegalArgumentException(
          "@BindsOptionalOf methods must not have parameters: " + method);
    }

    Annotation[] methodAnnotations = method.getDeclaredAnnotations();
    Annotation qualifier = findQualifier(methodAnnotations);
    Key dependency = Key.of(qualifier, method.getReturnType());
    return new LinkRequest(new Key[] { dependency }, new boolean[] { true });
  }

  @Override
  public LinkedBinding<?> link(LinkedBinding<?>[] dependencies) {
    return new LinkedOptionalBinding<>(dependencies[0]);
  }
}
