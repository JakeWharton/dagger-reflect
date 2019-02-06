package dagger.reflect;

import dagger.reflect.Binding.LinkedBinding;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.inject.Provider;

final class LinkedMapOfValueBinding<K, V> extends LinkedBinding<Map<K, V>> {
  private final LinkedBinding<Map<K, Provider<V>>> mapOfProviderBinding;

  LinkedMapOfValueBinding(LinkedBinding<Map<K, Provider<V>>> mapOfProviderBinding) {
    this.mapOfProviderBinding = mapOfProviderBinding;
  }

  @Override
  public Map<K, V> get() {
    Map<K, Provider<V>> mapOfProvider = mapOfProviderBinding.get();
    assert mapOfProvider != null;

    Map<K, V> mapOfValue = new LinkedHashMap<>(mapOfProvider.size());
    for (Map.Entry<K, Provider<V>> entry : mapOfProvider.entrySet()) {
      mapOfValue.put(entry.getKey(), entry.getValue().get());
    }
    return mapOfValue;
  }
}
