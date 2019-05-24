package dagger.reflect;

import dagger.reflect.Binding.LinkedBinding;
import javax.inject.Provider;

final class LinkedProviderBinding<T> extends LinkedBinding<Provider<T>> {
  private final Scope scope;
  private final Key key;

  LinkedProviderBinding(Scope scope, Key key) {
    this.scope = scope;
    this.key = key;
  }

  @Override public Provider<T> get() {
    return new ScopeKeyedProvider<>(scope, key);
  }
}
