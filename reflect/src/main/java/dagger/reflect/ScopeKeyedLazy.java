package dagger.reflect;

import dagger.Lazy;
import org.jetbrains.annotations.Nullable;

final class ScopeKeyedLazy<T> implements Lazy<T> {
  private static final Object UNINITIALIZED = new Object();

  private final Scope scope;
  private final Key key;
  private volatile @Nullable Object value = UNINITIALIZED;

  ScopeKeyedLazy(Scope scope, Key key) {
    this.scope = scope;
    this.key = key;
  }

  @SuppressWarnings("unchecked") // We trust Scope to return correct binding. Value set before cast.
  @Override public @Nullable T get() {
    Object value = this.value;
    if (value == UNINITIALIZED) {
      synchronized (this) {
        value = this.value;
        if (value == UNINITIALIZED) {
          value = this.value = scope.getBinding(key).get();
        }
      }
    }
    return (T) value;
  }
}
