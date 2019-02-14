package dagger.reflect;

import java.util.LinkedHashMap;
import java.util.Map;

final class LinkedMapBinding<K, V> extends Binding.LinkedBinding<Map<K, V>> {
  private final Map<K, LinkedBinding<V>> entryBindings;

  LinkedMapBinding(Map<K, LinkedBinding<V>> entryBindings) {
    this.entryBindings = entryBindings;
  }

  @Override public Map<K, V> get() {
    Map<K, V> map = new LinkedHashMap<>(entryBindings.size());
    for (Map.Entry<K, LinkedBinding<V>> elementBinding : entryBindings.entrySet()) {
      map.put(elementBinding.getKey(), elementBinding.getValue().get());
    }
    return map;
  }
}
