package dagger.reflect;

import dagger.reflect.Binding.UnlinkedBinding;
import java.util.LinkedHashMap;
import java.util.Map;

final class UnlinkedMapBinding extends UnlinkedBinding {
  private final Map<Object, Binding> entryBindings;

  UnlinkedMapBinding(Map<Object, Binding> entryBindings) {
    this.entryBindings = entryBindings;
  }

  @Override public LinkedBinding<?> link(Linker linker) {
    Map<Object, LinkedBinding<Object>> linkedBindings = new LinkedHashMap<>(entryBindings.size());
    for (Map.Entry<Object, Binding> entryBinding : entryBindings.entrySet()) {
      LinkedBinding<Object> linkedBinding =
          (LinkedBinding<Object>) entryBinding.getValue().link(linker);
      linkedBindings.put(entryBinding.getKey(), linkedBinding);
    }
    return new LinkedMapBinding<>(linkedBindings);
  }

  @Override public String toString() {
    return "Set" + entryBindings;
  }
}
