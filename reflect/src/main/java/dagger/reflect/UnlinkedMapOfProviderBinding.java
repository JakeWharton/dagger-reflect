package dagger.reflect;

import dagger.reflect.Binding.UnlinkedBinding;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.inject.Provider;

final class UnlinkedMapOfProviderBinding extends UnlinkedBinding {
  private final Map<Object, Binding> entryBindings;

  UnlinkedMapOfProviderBinding(Map<Object, Binding> entryBindings) {
    this.entryBindings = entryBindings;
  }

  @Override
  public LinkedBinding<?> link(Linker linker, Scope scope) {
    Map<Object, Provider<?>> mapOfProviders = new LinkedHashMap<>(entryBindings.size());
    for (Map.Entry<Object, Binding> entryBinding : entryBindings.entrySet()) {
      LinkedBinding<?> binding = entryBinding.getValue().link(linker, scope);
      mapOfProviders.put(entryBinding.getKey(), binding);
    }
    return new LinkedInstanceBinding<>(mapOfProviders);
  }

  @Override
  public String toString() {
    return "Map" + entryBindings;
  }
}
