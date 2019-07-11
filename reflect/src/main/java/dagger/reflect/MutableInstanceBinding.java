package dagger.reflect;

import dagger.reflect.Binding.LinkedBinding;
import org.jetbrains.annotations.Nullable;

final class MutableInstanceBinding<T> extends LinkedBinding<T> {
  private @Nullable T value;

  void setInstance(T value) {
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
