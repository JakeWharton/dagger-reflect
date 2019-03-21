package dagger.reflect;

import dagger.reflect.Binding.UnlinkedBinding;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

final class UnlinkedSetBinding extends UnlinkedBinding {
  private final List<Binding> elementBindings;
  private final List<Binding> elementsBindings;

  UnlinkedSetBinding(List<Binding> elementBindings, List<Binding> elementsBindings) {
    this.elementBindings = elementBindings;
    this.elementsBindings = elementsBindings;
  }

  @Override public LinkedBinding<?> link(Linker linker) {
    List<LinkedBinding<Object>> linkedElementBindings = new ArrayList<>(elementBindings.size());
    for (Binding elementBinding : elementBindings) {
      linkedElementBindings.add((LinkedBinding<Object>) elementBinding.link(linker));
    }
    List<LinkedBinding<Set<Object>>> linkedElementsBindings = new ArrayList<>(elementsBindings.size());
    for (Binding elementsBinding : elementsBindings) {
      linkedElementsBindings.add((LinkedBinding<Set<Object>>) elementsBinding.link(linker));
    }
    return new LinkedSetBinding<>(linkedElementBindings, linkedElementsBindings);
  }

  @Override public String toString() {
    return "Set" + elementBindings;
  }
}
