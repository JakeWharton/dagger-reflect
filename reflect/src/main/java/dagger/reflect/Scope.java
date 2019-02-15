package dagger.reflect;

import dagger.reflect.Binding.LinkedBinding;
import dagger.reflect.Binding.UnlinkedBinding;
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
    return Linker.link(bindings, key, (UnlinkedBinding) binding);
  }
}
