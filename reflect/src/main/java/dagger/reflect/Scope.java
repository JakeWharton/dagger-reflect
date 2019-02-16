package dagger.reflect;

import dagger.reflect.Binding.LinkedBinding;
import dagger.reflect.Binding.UnlinkedBinding;
import javax.inject.Provider;
import org.jetbrains.annotations.Nullable;

final class Scope {
  private final BindingMap bindings;
  private final @Nullable Scope parent;

  Scope(BindingMap bindings, @Nullable Scope parent) {
    this.bindings = bindings;
    this.parent = parent;
  }

  Provider<?> getProvider(Key key) {
    Binding binding = bindings.get(key);
    if (binding instanceof LinkedBinding<?>) {
      return (Provider<?>) binding;
    }
    if (binding != null) {
      return Linker.link(bindings, key, (UnlinkedBinding) binding);
    }
    if (parent != null) {
      return parent.getProvider(key);
    }
    throw new IllegalArgumentException("No provider available for " + key);
  }
}
