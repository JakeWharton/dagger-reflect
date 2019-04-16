package dagger.reflect;

import dagger.MembersInjector;
import dagger.reflect.Binding.UnlinkedBinding;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

import static dagger.reflect.Reflection.findQualifier;

final class UnlinkedJustInTimeBinding<T> extends UnlinkedBinding {
  private final Class<T> cls;
  private final Constructor<T> constructor;

  UnlinkedJustInTimeBinding(Class<T> cls, Constructor<T> constructor) {
    this.cls = cls;
    this.constructor = constructor;
  }

  @Override public LinkedBinding<?> link(Linker linker, Scope scope) {
    Type[] parameterTypes = constructor.getGenericParameterTypes();
    Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();

    LinkedBinding<?>[] bindings = new LinkedBinding<?>[parameterTypes.length];
    for (int i = 0; i < parameterTypes.length; i++) {
      Key key = Key.of(findQualifier(parameterAnnotations[i]), parameterTypes[i]);
      bindings[i] = linker.get(key);
    }

    MembersInjector<T> membersInjector = ReflectiveMembersInjector.create(cls, scope);

    return new LinkedJustInTimeBinding<>(constructor, bindings, membersInjector);
  }

  @Override public String toString() {
    return "@Inject[" + cls.getName() + ".<init>(…)]";
  }
}
