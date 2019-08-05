package dagger.reflect;

import dagger.reflect.Binding.UnlinkedBinding;
import java.util.Map;
import javax.inject.Provider;

final class UnlinkedMapOfValueBinding extends UnlinkedBinding {
  private final Key mapOfProviderKey;

  UnlinkedMapOfValueBinding(Key mapOfProviderKey) {
    this.mapOfProviderKey = mapOfProviderKey;
  }

  @Override
  public LinkedBinding<?> link(Linker linker, Scope scope) {
    // Assume that mapOfProviderKey is Map<K, Provider<V>> and linker returns the correct Binding.
    @SuppressWarnings("unchecked")
    LinkedBinding<Map<Object, Provider<Object>>> mapOfProviderBinding =
        (LinkedBinding<Map<Object, Provider<Object>>>) linker.get(mapOfProviderKey);
    return new LinkedMapOfValueBinding<>(mapOfProviderBinding);
  }

  @Override
  public String toString() {
    return "Map" + mapOfProviderKey;
  }
}
