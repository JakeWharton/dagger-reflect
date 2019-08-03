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

  @Override
  public LinkedBinding<?> link(Linker linker, Scope scope) {
    List<LinkedBinding<Object>> linkedElementBindings = new ArrayList<>(elementBindings.size());
    for (Binding elementBinding : elementBindings) {
      @SuppressWarnings("unchecked")
      LinkedBinding<Object> binding = (LinkedBinding<Object>) elementBinding.link(linker, scope);
      linkedElementBindings.add(binding);
    }

    List<LinkedBinding<Set<Object>>> linkedElementsBindings =
        new ArrayList<>(elementsBindings.size());
    for (Binding elementsBinding : elementsBindings) {
      @SuppressWarnings("unchecked")
      LinkedBinding<Set<Object>> bindings =
          (LinkedBinding<Set<Object>>) elementsBinding.link(linker, scope);
      linkedElementsBindings.add(bindings);
    }

    // `elementBinding` and `elementBindings` came from the same key so we can use Object as T;
    // hence their types will match, this is why unchecked warnings are OK above.
    return new LinkedSetBinding<>(linkedElementBindings, linkedElementsBindings);
  }

  @Override
  public String toString() {
    return "Set" + elementBindings;
  }
}
