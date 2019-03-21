package dagger.reflect;

import dagger.reflect.Binding.UnlinkedBinding;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import static dagger.reflect.Reflection.findQualifier;

final class UnlinkedBindsBinding extends UnlinkedBinding {
  private final Method method;

  UnlinkedBindsBinding(Method method) {
    this.method = method;
  }

  @Override public LinkedBinding<?> link(Linker linker, Scope scope) {
    Type[] parameterTypes = method.getGenericParameterTypes();
    if (parameterTypes.length != 1) {
      throw new IllegalArgumentException(
          "@Binds methods must have a single parameter: " + method);
    }
    Annotation[][] parameterAnnotations = method.getParameterAnnotations();
    Key dependency = Key.of(findQualifier(parameterAnnotations[0]), parameterTypes[0]);

    return linker.get(dependency);
  }

  @Override public String toString() {
    return "@Binds[" + method.getDeclaringClass().getName() + '.' + method.getName() + "(…)]";
  }
}
