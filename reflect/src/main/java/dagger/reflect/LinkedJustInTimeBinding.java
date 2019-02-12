package dagger.reflect;

import dagger.reflect.Binding.LinkedBinding;
import java.lang.reflect.Constructor;

import static dagger.reflect.Reflection.tryInstantiate;

public final class LinkedJustInTimeBinding<T> extends LinkedBinding<T> {
  private final Constructor<T> constructor;
  private final LinkedBinding<?>[] dependencies;

  LinkedJustInTimeBinding(Constructor<T> constructor, LinkedBinding<?>[] dependencies) {
    this.constructor = constructor;
    this.dependencies = dependencies;
  }

  @Override public T get() {
    Object[] arguments = new Object[dependencies.length];
    for (int i = 0; i < dependencies.length; i++) {
      arguments[i] = dependencies[i].get();
    }
    return tryInstantiate(constructor, arguments);
  }

  @Override public String toString() {
    return "@Inject[" + constructor.getDeclaringClass().getName() + ".<init>(…)]";
  }
}
