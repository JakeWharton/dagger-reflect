package dagger.reflect;

import dagger.reflect.Binding.LinkedBinding;
import dagger.reflect.Binding.UnlinkedBinding;
import javax.inject.Provider;
import org.jetbrains.annotations.Nullable;

final class Scope {
  private final BindingMap bindings;
  private final JustInTimeBindingFactory jitBindingFactory;
  private final @Nullable Scope parent;

  Scope(BindingMap bindings, JustInTimeBindingFactory jitBindingFactory, @Nullable Scope parent) {
    this.bindings = bindings;
    this.jitBindingFactory = jitBindingFactory;
    this.parent = parent;
  }

  /**
   * Look for a linked binding for {@code key} in this scope or anywhere in the parent scope chain.
   * If an unlinked binding is found for the key, perform linking before returning it.
   */
  private @Nullable LinkedBinding<?> findBinding(Key key) {
    Binding binding = bindings.get(key);
    if (binding != null) {
      return binding instanceof LinkedBinding<?>
          ? (LinkedBinding<?>) binding
          :  Linker.link(bindings, key, (UnlinkedBinding) binding);
    }

    return parent != null
        ? parent.findBinding(key)
        : null;
  }

  Provider<?> getProvider(Key key) {
    LinkedBinding<?> binding = findBinding(key);
    if (binding != null) {
      return binding;
    }

    Binding jitBinding = jitBindingFactory.create(key);
    if (jitBinding != null) {
      // TODO figure out if scoped and walk up hierarchy looking for a matching scope.

      jitBinding = bindings.putIfAbsent(key, jitBinding);
      return jitBinding instanceof LinkedBinding<?>
          ? (LinkedBinding<?>) jitBinding
          : Linker.link(bindings, key, (UnlinkedBinding) jitBinding);
    }

    throw new IllegalArgumentException("No provider available for " + key);
  }
}
