package dagger.reflect;

import dagger.reflect.Binding.LinkedBinding;
import org.jetbrains.annotations.Nullable;

final class LinkedInstanceBinding<T> extends LinkedBinding<T> {
  private final @Nullable T value;

  LinkedInstanceBinding(@Nullable T value) {
    this.value = value;
  }

  @Override
  public @Nullable T get() {
    return value;
  }

  @Override
  public String toString() {
    return "@BindsInstance[" + value + ']';
  }
}
