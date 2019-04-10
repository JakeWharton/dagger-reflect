package dagger.reflect;

import dagger.reflect.Binding.LinkedBinding;
import dagger.reflect.Binding.UnlinkedBinding;
import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

final class Scope {
  final BindingMap bindings;
  private final JustInTimeLookup.Factory jitLookupFactory;
  /** The annotation denoting {@linkplain javax.inject.Scope scoped} bindings for this instance. */
  private final @Nullable Annotation annotation;
  private final @Nullable Scope parent;

  private Scope(BindingMap bindings, JustInTimeLookup.Factory jitLookupFactory,
      @Nullable Annotation annotation, @Nullable Scope parent) {
    this.bindings = bindings;
    this.jitLookupFactory = jitLookupFactory;
    this.annotation = annotation;
    this.parent = parent;
  }

  LinkedBinding<?> getBinding(Key key) {
    LinkedBinding<?> binding = findBinding(key);
    if (binding != null) {
      return binding;
    }

    JustInTimeLookup jitLookup = jitLookupFactory.create(key);
    if (jitLookup != null) {
      LinkedBinding<?> jitBinding = putJitBinding(key, jitLookup);
      if (jitBinding == null) {
        throw new IllegalStateException(); // TODO nice error message with scope chain
      }
      return jitBinding;
    }

    throw new IllegalArgumentException("No provider available for " + key);
  }

  /**
   * Look for a linked binding for {@code key} in this scope or anywhere in the parent scope chain.
   * If an unlinked binding is found for the key, perform linking before returning it.
   */
  private @Nullable LinkedBinding<?> findBinding(Key key) {
    Binding binding = bindings.get(key);
    if (binding != null) {
      return binding instanceof LinkedBinding<?>
          ? (LinkedBinding<?>) binding
          :  Linker.link(this, key, (UnlinkedBinding) binding);
    }

    return parent != null
        ? parent.findBinding(key)
        : null;
  }

  /**
   * Attempt to insert a binding for {@code key} as a result of a just-in-time lookup.
   * <p>
   * If {@code lookup} does not contain a scoping annotation or it contains a scoping annotation
   * which matches the one for this scope, the binding will be inserted, linked, and returned.
   * Otherwise, the parent scope (if any) will be recursively checked. If no matching scope
   * annotation is found by traversing the parents null will be returned.
   */
  private @Nullable LinkedBinding<?> putJitBinding(Key key, JustInTimeLookup lookup) {
    Binding jitBinding = lookup.binding;

    Annotation scope = lookup.scope;
    if (scope != null) {
      if (!scope.equals(annotation)) {
        return parent != null
            ? parent.putJitBinding(key, lookup)
            : null;
      }

      jitBinding = jitBinding.asScoped();
    }

    Binding binding = bindings.putIfAbsent(key, jitBinding);
    return binding instanceof LinkedBinding<?>
        ? (LinkedBinding<?>) binding
        : Linker.link(this, key, (UnlinkedBinding) binding);
  }

  static final class Builder {
    private final @Nullable Scope parent;
    final @Nullable Annotation annotation;
    private final BindingMap.Builder bindings = new BindingMap.Builder();
    private JustInTimeLookup.Factory jitLookupFactory = JustInTimeLookup.Factory.NONE;

    Builder(@Nullable Scope parent, @Nullable Annotation annotation) {
      if (annotation != null && parent != null) {
        if (parent.annotation == null) {
          throw new IllegalStateException("Scope " + annotation + " may not depend on unscoped");
        }

        // Traverse ancestry chain looking for a duplicate annotation declaration.
        for (Scope ancestor = parent; ancestor != null; ancestor = ancestor.parent) {
          if (annotation.equals(ancestor.annotation)) {
            StringBuilder message = new StringBuilder("Detected scope annotation cycle:\n  * ")
                .append(annotation)
                .append('\n');
            // Re-traverse the ancestry from our parent up to the offending ancestor for the chain.
            for (Scope visit = parent; visit != ancestor; visit = visit.parent) {
              if (visit == null) throw new AssertionError(); // Checked by outer loop.
              message.append("  * ").append(visit.annotation).append('\n');
            }
            message.append("  * ").append(ancestor.annotation);
            throw new IllegalStateException(message.toString());
          }
        }
      }
      this.parent = parent;
      this.annotation = annotation;
    }

    Builder justInTimeLookupFactory(JustInTimeLookup.Factory jitLookupFactory) {
      if (jitLookupFactory == null) throw new NullPointerException("jitLookupFactory == null");
      this.jitLookupFactory = jitLookupFactory;
      return this;
    }

    Builder addBinding(Key key, Binding binding) {
      if (key == null) throw new NullPointerException("key == null");
      if (binding == null) throw new NullPointerException("binding == null");

      bindings.add(key, binding);
      return this;
    }

    /**
     * Adds a new element into the set specified by {@code key}.
     *
     * @param key The key defining the set into which this element will be added. The raw class of
     * the {@linkplain Key#type() type} must be {@link Set Set.class}.
     * @param elementBinding The binding for the new element. The instance produced by this binding
     * must be an instance of the {@code E} type parameter of {@link Set} specified
     * {@linkplain Key#type() in <code>key</code>}.
     */
    Builder addBindingIntoSet(Key key, Binding elementBinding) {
      if (key == null) throw new NullPointerException("key == null");
      if (elementBinding == null) throw new NullPointerException("elementBinding == null");
      if (Types.getRawType(key.type()) != Set.class) {
        throw new IllegalArgumentException("key.type() must be Set");
      }

      bindings.addIntoSet(key, elementBinding);
      return this;
    }

    /**
     * Adds a new element into the set specified by {@code key}.
     *
     * @param key The key defining the set into which this element will be added. The raw class of
     * the {@linkplain Key#type() type} must be {@link Set Set.class}.
     * @param elementsBinding The binding for the elements. The instance produced by this binding
     * must be an instance of {@code Set<E>} matching {@link Key#type() key.type()}.
     */
    Builder addBindingElementsIntoSet(Key key, Binding elementsBinding) {
      if (key == null) throw new NullPointerException("key == null");
      if (elementsBinding == null) throw new NullPointerException("elementsBinding == null");
      if (Types.getRawType(key.type()) != Set.class) {
        throw new IllegalArgumentException("key.type() must be Set");
      }

      bindings.addElementsIntoSet(key, elementsBinding);
      return this;
    }

    /**
     * Adds a new entry into the map specified by {@code key}.
     *
     * @param key The key defining the map in which this entry will be added. The raw class of the
     * {@linkplain Key#type() type} must be {@link Map Map.class}.
     * @param entryKey The key of the new map entry. The argument must be an instance of the
     * {@code K} type parameter of {@link Map} specified
     * {@linkplain Key#type() in the <code>key</code>}.
     * @param entryValueBinding The value binding of the new map entry. The instance produced by
     * this binding must be an instance of the {@code V} type parameter of {@link Map} specified
     * {@linkplain Key#type()} in the <code>key</code>}.
     */
    Builder addBindingIntoMap(Key key, Object entryKey, Binding entryValueBinding) {
      if (key == null) throw new NullPointerException("key == null");
      if (entryKey == null) throw new NullPointerException("entryKey == null");
      if (entryValueBinding == null) throw new NullPointerException("entryValueBinding == null");
      if (Types.getRawType(key.type()) != Map.class) {
        throw new IllegalArgumentException("key.type() must be Map");
      }

      bindings.addIntoMap(key, entryKey, entryValueBinding);
      return this;
    }

    Builder addInstance(Key key, @Nullable Object instance) {
      return addBinding(key, new LinkedInstanceBinding<>(instance));
    }

    Builder addModule(Object module) {
      ReflectiveModuleParser.parse(module.getClass(), module, this);
      return this;
    }

    Builder addModule(Class<?> module) {
      ReflectiveModuleParser.parse(module, null, this);
      return this;
    }

    Builder addDependency(Class<?> cls, Object instance) {
      ReflectiveDependencyParser.parse(cls, instance, this);
      return this;
    }

    Scope build() {
      return new Scope(bindings.build(), jitLookupFactory, annotation, parent);
    }
  }
}
