package dagger.reflect;

import dagger.reflect.Binding.UnlinkedBinding;

final class UnlinkedScopedBinding extends UnlinkedBinding {
  private final UnlinkedBinding unlinkedBinding;

  UnlinkedScopedBinding(UnlinkedBinding unlinkedBinding) {
    this.unlinkedBinding = unlinkedBinding;
  }

  @Override public LinkedBinding<?> link(Linker linker, Scope scope) {
    return new LinkedScopedBinding<>(unlinkedBinding.link(linker, scope));
  }
}
