package dagger.reflect;

import dagger.Lazy;
import org.jetbrains.annotations.Nullable;

final class ScopeKeyedLazy<T> implements Lazy<T> {
  private final Scope scope;
  private final Key key;

  /**
   * The cached value produced from calling the binding returned by {@link #scope} for {@link #key}.
   * Is set to {@code this} to indicate an uninitialized value because the binding may produce null
   * when called.
   */
  private volatile @Nullable Object value = this;

  ScopeKeyedLazy(Scope scope, Key key) {
    this.scope = scope;
    this.key = key;
  }

  @SuppressWarnings("unchecked") // We trust Scope to return correct binding.
  @Override
  @Nullable
  public T get() {
    Object value = this.value;
    if (value == this) {
      synchronized (this) {
        value = this.value;
        if (value == this) {
          value = this.value = scope.getBinding(key).get();
        }
      }
    }
    return (T) value;
  }
}
