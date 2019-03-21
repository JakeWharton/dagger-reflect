package dagger.reflect;

import dagger.reflect.Binding.UnlinkedBinding;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import static dagger.reflect.Reflection.findQualifier;

public final class UnlinkedJavaOptionalBinding extends UnlinkedBinding {
  private final Method method;

  UnlinkedJavaOptionalBinding(Method method) {
    this.method = method;
  }

  @Override public LinkedBinding<?> link(Linker linker, Scope scope) {
    Type[] parameterTypes = method.getGenericParameterTypes();
    if (parameterTypes.length != 0) {
      throw new IllegalArgumentException(
          "@BindsOptionalOf methods must not have parameters: " + method);
    }

    Annotation[] methodAnnotations = method.getDeclaredAnnotations();
    Annotation qualifier = findQualifier(methodAnnotations);
    Key key = Key.of(qualifier, method.getReturnType());

    LinkedBinding<?> dependency = linker.find(key);
    return new LinkedJavaOptionalBinding<>(dependency);
  }

  @Override public String toString() {
    return "@Optional[" + method.getDeclaringClass().getName() + '.' + method.getName() + "(…)]";
  }
}
