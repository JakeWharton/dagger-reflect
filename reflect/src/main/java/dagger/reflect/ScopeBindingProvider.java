package dagger.reflect;

import dagger.reflect.Binding.LinkedBinding;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Provider;

final class ScopeBindingProvider<T> implements Provider<T> {
  private final Scope scope;
  private final Binding binding;
  private final AtomicReference<LinkedBinding<T>> linkedRef;

  ScopeBindingProvider(Scope scope, Binding binding) {
    this.scope = scope;
    this.binding = binding;
    this.linkedRef =
        new AtomicReference<>(
            binding instanceof LinkedBinding<?> ? (LinkedBinding<T>) binding : null);
  }

  @Override
  public T get() {
    LinkedBinding<T> linked = linkedRef.get();
    if (linked == null) {
      linked = (LinkedBinding<T>) binding.link(new Linker(scope), scope);

      LinkedBinding<T> replaced = linkedRef.getAndSet(linked);
      if (replaced != null) {
        linked = replaced; // You raced another thread and lost.
      }
    }
    return linked.get();
  }
}
