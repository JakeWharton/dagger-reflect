package dagger.reflect;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class LinkedSetBinding<T> extends Binding.LinkedBinding<Set<T>> {
  private final List<LinkedBinding<T>> elementBindings;
  private final List<LinkedBinding<Set<T>>> elementsBindings;

  LinkedSetBinding(
      List<LinkedBinding<T>> elementBindings, List<LinkedBinding<Set<T>>> elementsBindings) {
    this.elementBindings = elementBindings;
    this.elementsBindings = elementsBindings;
  }

  @Override
  public Set<T> get() {
    Set<T> elements = new LinkedHashSet<>();
    for (LinkedBinding<T> elementBinding : elementBindings) {
      elements.add(elementBinding.get());
    }
    for (LinkedBinding<Set<T>> elementsBinding : elementsBindings) {
      elements.addAll(elementsBinding.get());
    }
    return elements;
  }
}
