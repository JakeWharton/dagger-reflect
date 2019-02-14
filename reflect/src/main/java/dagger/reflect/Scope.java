package dagger.reflect;

import dagger.reflect.Binding.LinkedBinding;
import javax.inject.Provider;

final class Scope {
  private final BindingMap bindings;

  Scope(BindingMap bindings) {
    this.bindings = bindings;
  }

  Provider<?> getProvider(Key key) {
    Binding binding = bindings.get(key);
    if (binding instanceof LinkedBinding<?>) {
      return (Provider<?>) binding;
    }
    if (binding == null) {
      throw new IllegalArgumentException("No provider available for " + key);
    }
    return Linker.getLinked(bindings, key);
  }
}
