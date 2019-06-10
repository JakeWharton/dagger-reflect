package dagger.reflect;

import dagger.reflect.Binding.UnlinkedBinding;
import java.util.Map;
import javax.inject.Provider;

final class UnlinkedMapOfLazyBinding extends UnlinkedBinding {
  private final Key mapOfProviderKey;

  UnlinkedMapOfLazyBinding(Key mapOfProviderKey) {
    this.mapOfProviderKey = mapOfProviderKey;
  }

  @Override public LinkedBinding<?> link(Linker linker, Scope scope) {
    LinkedBinding<Map<Object, Provider<Object>>> mapOfProviderBinding =
        (LinkedBinding<Map<Object, Provider<Object>>>) linker.get(mapOfProviderKey);
    return new LinkedMapOfLazyBinding<>(mapOfProviderBinding);
  }

  @Override public String toString() {
    return "Map" + mapOfProviderKey;
  }
}
