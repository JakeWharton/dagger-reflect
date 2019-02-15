package dagger.reflect;

import dagger.reflect.Binding.UnlinkedBinding;
import java.util.ArrayList;
import java.util.List;

final class UnlinkedSetBinding extends UnlinkedBinding {
  private final List<Binding> elementBindings;

  UnlinkedSetBinding(List<Binding> elementBindings) {
    this.elementBindings = elementBindings;
  }

  @Override public LinkedBinding<?> link(Linker linker) {
    List<LinkedBinding<Object>> linkedBindings = new ArrayList<>(elementBindings.size());
    for (Binding elementBinding : elementBindings) {
      linkedBindings.add((LinkedBinding<Object>) elementBinding.link(linker));
    }
    return new LinkedSetBinding<>(linkedBindings);
  }

  @Override public String toString() {
    return "Map" + elementBindings;
  }
}
