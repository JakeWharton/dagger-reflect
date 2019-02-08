package dagger.reflect;

import dagger.reflect.Binding.LinkedBinding;

final class LinkedInstanceBinding<T> extends LinkedBinding<T> {
  private final T value;

  LinkedInstanceBinding(T value) {
    this.value = value;
  }

  @Override public T get() {
    return value;
  }

  @Override public String toString() {
    return "@BindsInstance[" + value + ']';
  }
}
