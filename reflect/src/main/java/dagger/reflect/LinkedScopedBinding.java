package dagger.reflect;

import dagger.reflect.Binding.LinkedBinding;
import org.jetbrains.annotations.Nullable;

final class LinkedScopedBinding<T> extends LinkedBinding<T> {
  private final LinkedBinding<T> binding;
  /**
   * The cached value produced from calling {@link #binding}. Is set to {@code this} to indicate an
   * uninitialized value because the binding may produce null when called.
   */
  private volatile @Nullable Object instance = this;

  LinkedScopedBinding(LinkedBinding<T> binding) {
    this.binding = binding;
  }

  @SuppressWarnings("unchecked") // Instance will only be of type T at the point of cast.
  @Override
  public @Nullable T get() {
    Object instance = this.instance;
    if (instance == this) {
      synchronized (this) {
        instance = this.instance;
        if (instance == this) {
          instance = this.instance = binding.get();
        }
      }
    }
    return (T) instance;
  }
}
