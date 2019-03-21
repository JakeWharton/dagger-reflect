package dagger.reflect;

import dagger.MembersInjector;
import dagger.reflect.Binding.LinkedBinding;
import java.lang.reflect.Constructor;

import static dagger.reflect.Reflection.tryInstantiate;

public final class LinkedJustInTimeBinding<T> extends LinkedBinding<T> {
  private final Constructor<T> constructor;
  private final LinkedBinding<?>[] dependencies;
  private final MembersInjector<T> membersInjector;

  LinkedJustInTimeBinding(Constructor<T> constructor, LinkedBinding<?>[] dependencies,
      MembersInjector<T> membersInjector) {
    this.constructor = constructor;
    this.dependencies = dependencies;
    this.membersInjector = membersInjector;
  }

  @Override public T get() {
    Object[] arguments = new Object[dependencies.length];
    for (int i = 0; i < dependencies.length; i++) {
      arguments[i] = dependencies[i].get();
    }
    T instance = tryInstantiate(constructor, arguments);
    membersInjector.injectMembers(instance);
    return instance;
  }

  @Override public String toString() {
    return "@Inject[" + constructor.getDeclaringClass().getName() + ".<init>(…)]";
  }
}
