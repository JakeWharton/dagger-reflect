package dagger.reflect;

import dagger.reflect.Binding.LinkedBinding;
import org.jetbrains.annotations.Nullable;

/**
 * A binding whose value will be set at a later time. A value must be set before the binding is
 * used.
 */
final class LinkedLateInstanceBinding<T> extends LinkedBinding<T> {
  private @Nullable T value;

  void setValue(T value) {
    if (this.value != null) throw new IllegalStateException();
    this.value = value;
  }

  @Override
  public T get() {
    T value = this.value;
    if (value == null) throw new IllegalStateException();
    return value;
  }
}
