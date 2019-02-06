package dagger.reflect;

import com.google.common.base.Optional;
import dagger.reflect.Binding.LinkedBinding;
import org.jetbrains.annotations.Nullable;

final class LinkedGuavaOptionalBinding<T> extends LinkedBinding<Optional<T>> {
  private final @Nullable LinkedBinding<T> dependency;

  LinkedGuavaOptionalBinding(@Nullable LinkedBinding<T> dependency) {
    this.dependency = dependency;
  }

  @Override
  public Optional<T> get() {
    if (dependency == null) {
      return Optional.absent();
    }
    T value = dependency.get();
    if (value == null) {
      throw new NullPointerException(
          dependency + " returned null which is not allowed for optional bindings");
    }
    return Optional.of(value);
  }

  @Override
  public String toString() {
    return "@BindsOptionalOf[" + dependency + ']';
  }
}
