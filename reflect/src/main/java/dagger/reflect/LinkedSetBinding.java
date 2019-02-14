package dagger.reflect;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

final class LinkedSetBinding<T> extends Binding.LinkedBinding<Set<T>> {
  private final List<LinkedBinding<T>> elementBindings;

  LinkedSetBinding(List<LinkedBinding<T>> elementBindings) {
    this.elementBindings = elementBindings;
  }

  @Override public Set<T> get() {
    Set<T> elements = new LinkedHashSet<>(elementBindings.size());
    for (LinkedBinding<T> elementBinding : elementBindings) {
      elements.add(elementBinding.get());
    }
    return elements;
  }
}
