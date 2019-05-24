package dagger.reflect;

import dagger.reflect.Binding.LinkedBinding;
import javax.inject.Provider;
import org.jetbrains.annotations.Nullable;

final class ScopeKeyedProvider<T> implements Provider<T> {
  private final Scope scope;
  private final Key key;
  private @Nullable LinkedBinding<T> binding;

  ScopeKeyedProvider(Scope scope, Key key) {
    this.scope = scope;
    this.key = key;
  }

  @SuppressWarnings("unchecked") // We trust Scope to return correct binding.
  @Override public @Nullable T get() {
    LinkedBinding<T> binding = this.binding;
    if (binding == null) {
      binding = this.binding = (LinkedBinding<T>) scope.getBinding(key);
    }
    return binding.get();
  }
}
