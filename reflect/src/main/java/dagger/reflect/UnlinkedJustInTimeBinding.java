package dagger.reflect;

import dagger.reflect.Binding.UnlinkedBinding;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import javax.inject.Inject;

import static dagger.reflect.DaggerReflect.notImplemented;
import static dagger.reflect.Reflection.findQualifier;

final class UnlinkedJustInTimeBinding<T> extends UnlinkedBinding {
  private final Class<T> cls;
  private final Constructor<T> constructor;

  UnlinkedJustInTimeBinding(Class<T> cls, Constructor<T> constructor) {
    this.cls = cls;
    this.constructor = constructor;
  }

  @Override public LinkedBinding<?> link(Linker linker, Scope scope) {
    // TODO field and method bindings? reuse some/all of reflective members injector somehow?
    Class<?> target = cls;
    while (target != Object.class) {
      for (Field field : target.getDeclaredFields()) {
        if (field.getAnnotation(Inject.class) != null) {
          throw notImplemented("@Inject on fields in just-in-time bindings");
        }
      }
      for (Method method : target.getDeclaredMethods()) {
        if (method.getAnnotation(Inject.class) != null) {
          throw notImplemented("@Inject on methods in just-in-time bindings");
        }
      }
      target = target.getSuperclass();
    }

    Type[] parameterTypes = constructor.getGenericParameterTypes();
    Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();

    LinkedBinding<?>[] bindings = new LinkedBinding<?>[parameterTypes.length];
    for (int i = 0; i < parameterTypes.length; i++) {
      Key key = Key.of(findQualifier(parameterAnnotations[i]), parameterTypes[i]);
      bindings[i] = linker.get(key);
    }

    return new LinkedJustInTimeBinding<>(constructor, bindings);
  }

  @Override public String toString() {
    return "@Inject[" + cls.getName() + ".<init>(…)]";
  }
}
