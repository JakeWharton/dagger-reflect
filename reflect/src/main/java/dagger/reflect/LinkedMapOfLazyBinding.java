package dagger.reflect;

import dagger.Lazy;
import dagger.reflect.Binding.LinkedBinding;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.inject.Provider;

final class LinkedMapOfLazyBinding<K, V> extends LinkedBinding<Map<K, Lazy<V>>> {
  private final LinkedBinding<Map<K, Provider<V>>> mapOfProviderBinding;

  LinkedMapOfLazyBinding(LinkedBinding<Map<K, Provider<V>>> mapOfProviderBinding) {
    this.mapOfProviderBinding = mapOfProviderBinding;
  }

  @Override public Map<K, Lazy<V>> get() {
    Map<K, Provider<V>> mapOfProvider = mapOfProviderBinding.get();
    assert mapOfProvider != null;

    Map<K, Lazy<V>> mapOfLazy = new LinkedHashMap<>(mapOfProvider.size());
    for (Map.Entry<K, Provider<V>> entry : mapOfProvider.entrySet()) {
      mapOfLazy.put(entry.getKey(), new DoubleChecked.OfProvider<>(entry.getValue()));
    }
    return mapOfLazy;
  }
}
