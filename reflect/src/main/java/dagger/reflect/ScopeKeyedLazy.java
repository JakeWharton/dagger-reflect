package dagger.reflect;

import org.jetbrains.annotations.Nullable;

final class ScopeKeyedLazy<T> extends DoubleChecked<T> {
  private final Scope scope;
  private final Key key;

  ScopeKeyedLazy(Scope scope, Key key) {
    this.scope = scope;
    this.key = key;
  }

  @SuppressWarnings("unchecked") // We trust Scope to return correct binding.
  @Override
  @Nullable
  T compute() {
    return (T) scope.getBinding(key).get();
  }
}
