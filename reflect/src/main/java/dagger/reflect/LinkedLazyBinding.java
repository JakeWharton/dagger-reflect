package dagger.reflect;

import dagger.Lazy;
import dagger.reflect.Binding.LinkedBinding;

final class LinkedLazyBinding<T> extends LinkedBinding<Lazy<T>> {
  private final Scope scope;
  private final Key key;

  LinkedLazyBinding(Scope scope, Key key) {
    this.scope = scope;
    this.key = key;
  }

  @Override public Lazy<T> get() {
    return new ScopeKeyedLazy<>(scope, key);
  }
}
