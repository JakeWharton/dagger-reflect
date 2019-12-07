package dagger.reflect;

import dagger.Lazy;
import dagger.reflect.Binding.LinkedBinding;
import dagger.reflect.Binding.UnlinkedBinding;
import dagger.reflect.TypeUtil.ParameterizedTypeImpl;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Provider;
import org.jetbrains.annotations.Nullable;

final class Scope {
  private final ConcurrentHashMap<Key, Binding> bindings;
  private final JustInTimeLookup.Factory jitLookupFactory;
  /** The annotations denoting {@linkplain javax.inject.Scope scoped} bindings for this instance. */
  private final Set<Annotation> annotations;

  private final @Nullable Scope parent;

  private Scope(
      ConcurrentHashMap<Key, Binding> bindings,
      JustInTimeLookup.Factory jitLookupFactory,
      Set<Annotation> annotations,
      @Nullable Scope parent) {
    this.bindings = bindings;
    this.jitLookupFactory = jitLookupFactory;
    this.annotations = annotations;
    this.parent = parent;
  }

  @Override
  public String toString() {
    return "Scope" + annotations;
  }

  LinkedBinding<?> getBinding(Key key) {
    LinkedBinding<?> binding = findBinding(key, null);
    if (binding != null) {
      return binding;
    }
    throw new IllegalArgumentException("No provider available for " + key);
  }

  /**
   * Look for a linked binding for {@code key} in this scope or anywhere in the parent scope chain.
   * If an unlinked binding is found for the key, perform linking before returning it. Bindings may
   * be just-in-time created to fulfil this request.
   *
   * @param linker An optional {@link Linker} to use. One will be created if null and needed.
   */
  @Nullable
  LinkedBinding<?> findBinding(Key key, @Nullable Linker linker) {
    Type keyType = key.type();
    if (keyType instanceof ParameterizedType) {
      ParameterizedType parameterizedKeyType = (ParameterizedType) keyType;
      Type rawKeyType = parameterizedKeyType.getRawType();
      if (rawKeyType == Provider.class) {
        Key realKey = Key.of(key.qualifier(), parameterizedKeyType.getActualTypeArguments()[0]);
        return new LinkedProviderBinding<>(this, realKey);
      }
      if (rawKeyType == Lazy.class) {
        Key realKey = Key.of(key.qualifier(), parameterizedKeyType.getActualTypeArguments()[0]);
        return new LinkedLazyBinding<>(this, realKey);
      }
    }

    LinkedBinding<?> binding = findExistingBinding(key, linker);
    if (binding != null) {
      return binding;
    }

    JustInTimeLookup jitLookup = jitLookupFactory.create(key);
    if (jitLookup != null) {
      LinkedBinding<?> jitBinding = putJitBinding(key, linker, jitLookup);
      if (jitBinding == null) {
        throw new IllegalStateException(
            "Unable to find binding for key=" + key + " with linker=" + linker);
      }
      return jitBinding;
    }

    return null;
  }

  /**
   * Look for an existing linked binding for {@code key} in this scope or anywhere in the parent
   * scope chain. If an unlinked binding is found for the key, perform linking before returning it.
   *
   * @param linker An optional {@link Linker} to use. One will be created if null and needed.
   */
  private @Nullable LinkedBinding<?> findExistingBinding(Key key, @Nullable Linker linker) {
    Binding binding = bindings.get(key);
    if (binding != null) {
      return binding instanceof LinkedBinding<?>
          ? (LinkedBinding<?>) binding
          : link(key, linker, (UnlinkedBinding) binding);
    }

    return parent != null ? parent.findExistingBinding(key, linker) : null;
  }

  /**
   * Attempt to insert a binding for {@code key} as a result of a just-in-time lookup.
   *
   * <p>If {@code lookup} does not contain a scoping annotation or it contains a scoping annotation
   * which matches the one for this scope, the binding will be inserted, linked, and returned.
   * Otherwise, the parent scope (if any) will be recursively checked. If no matching scope
   * annotation is found by traversing the parents null will be returned.
   */
  private @Nullable LinkedBinding<?> putJitBinding(
      Key key, @Nullable Linker linker, JustInTimeLookup lookup) {
    Binding jitBinding = lookup.binding;

    Annotation scope = lookup.scope;
    if (scope != null) {
      if (!annotations.contains(scope)) {
        return parent != null ? parent.putJitBinding(key, linker, lookup) : null;
      }

      jitBinding = jitBinding.asScoped();
    }

    Binding replaced = bindings.putIfAbsent(key, jitBinding);
    Binding binding =
        replaced != null
            ? replaced // You raced another thread and lost.
            : jitBinding;

    return binding instanceof LinkedBinding<?>
        ? (LinkedBinding<?>) binding
        : link(key, linker, (UnlinkedBinding) binding);
  }

  private LinkedBinding<?> link(Key key, @Nullable Linker linker, UnlinkedBinding binding) {
    if (linker == null) {
      linker = new Linker(this);
    }
    LinkedBinding<?> linkedBinding = linker.link(key, binding);

    if (!bindings.replace(key, binding, linkedBinding)) {
      // If replace() returned false we raced another thread and lost. Return the winner.
      LinkedBinding<?> race = (LinkedBinding<?>) bindings.get(key);
      if (race == null) throw new AssertionError();
      return race;
    }
    return linkedBinding;
  }

  static final class Builder {
    private final @Nullable Scope parent;
    final Set<Annotation> annotations;
    private final Map<Key, Binding> keyToBinding = new LinkedHashMap<>();
    private final Map<Key, SetBindings> keyToSetBindings = new LinkedHashMap<>();
    private final Map<Key, Map<Object, Binding>> keyToMapBindings = new LinkedHashMap<>();
    private JustInTimeLookup.Factory jitLookupFactory = JustInTimeLookup.Factory.NONE;

    Builder(@Nullable Scope parent, Set<Annotation> annotations) {
      if (!annotations.isEmpty() && parent != null) {
        if (parent.annotations.isEmpty()) {
          throw new IllegalStateException(
              "Scope with annotations " + annotations + " may not depend on unscoped");
        }

        // Traverse ancestry chain looking for a duplicate annotation declaration.
        for (Scope ancestor = parent; ancestor != null; ancestor = ancestor.parent) {
          boolean duplicateScope = false;
          for (Annotation ancestorAnnotation : ancestor.annotations) {
            if (annotations.contains(ancestorAnnotation)) {
              duplicateScope = true;
              break;
            }
          }
          if (duplicateScope) {
            StringBuilder message =
                new StringBuilder("Detected scope annotation cycle:\n  * ")
                    .append(annotations)
                    .append('\n');
            // Re-traverse the ancestry from our parent up to the offending ancestor for the chain.
            for (Scope visit = parent; visit != ancestor; visit = visit.parent) {
              if (visit == null) throw new AssertionError(); // Checked by outer loop.
              message.append("  * ").append(visit.annotations).append('\n');
            }
            message.append("  * ").append(ancestor.annotations);
            throw new IllegalStateException(message.toString());
          }
        }
      }
      this.parent = parent;
      this.annotations = annotations;
    }

    Builder justInTimeLookupFactory(JustInTimeLookup.Factory jitLookupFactory) {
      if (jitLookupFactory == null) throw new NullPointerException("jitLookupFactory == null");
      this.jitLookupFactory = jitLookupFactory;
      return this;
    }

    Builder addBinding(Key key, Binding binding) {
      if (key == null) throw new NullPointerException("key == null");
      if (binding == null) throw new NullPointerException("binding == null");

      Binding replaced = keyToBinding.put(key, binding);
      if (replaced != null) {
        throw new IllegalStateException(
            "Duplicate binding for " + key + ": " + replaced + " and " + binding);
      }

      return this;
    }

    /**
     * Create an empty set binding specified by {@code key} if it does not already exist.
     *
     * @param key The key defining the set. The raw class of the {@linkplain Key#type() type} must
     *     be {@link Set Set.class}.
     */
    Builder createSetBinding(Key key) {
      if (key == null) throw new NullPointerException("key == null");
      if (Types.getRawType(key.type()) != Set.class) {
        throw new IllegalArgumentException("key.type() must be Set");
      }

      if (!keyToSetBindings.containsKey(key)) {
        keyToSetBindings.put(key, new SetBindings());
      }

      return this;
    }

    /**
     * Adds a new element into the set specified by {@code key}.
     *
     * @param key The key defining the set into which this element will be added. The raw class of
     *     the {@linkplain Key#type() type} must be {@link Set Set.class}.
     * @param elementBinding The binding for the new element. The instance produced by this binding
     *     must be an instance of the {@code E} type parameter of {@link Set} specified {@linkplain
     *     Key#type() in <code>key</code>}.
     */
    Builder addBindingIntoSet(Key key, Binding elementBinding) {
      if (key == null) throw new NullPointerException("key == null");
      if (elementBinding == null) throw new NullPointerException("elementBinding == null");
      if (Types.getRawType(key.type()) != Set.class) {
        throw new IllegalArgumentException("key.type() must be Set");
      }

      SetBindings setBindings = keyToSetBindings.get(key);
      //noinspection Java8MapApi Supporting old Android API levels.
      if (setBindings == null) {
        setBindings = new SetBindings();
        keyToSetBindings.put(key, setBindings);
      }
      setBindings.elementBindings.add(elementBinding);

      return this;
    }

    /**
     * Adds a new set of elements into the set specified by {@code key}.
     *
     * @param key The key defining the set into which this element will be added. The raw class of
     *     the {@linkplain Key#type() type} must be {@link Set Set.class}.
     * @param elementsBinding The binding for the elements. The instance produced by this binding
     *     must be an instance of {@code Set<E>} matching {@link Key#type() key.type()}.
     */
    Builder addBindingElementsIntoSet(Key key, Binding elementsBinding) {
      if (key == null) throw new NullPointerException("key == null");
      if (elementsBinding == null) throw new NullPointerException("elementsBinding == null");
      if (Types.getRawType(key.type()) != Set.class) {
        throw new IllegalArgumentException("key.type() must be Set");
      }

      SetBindings setBindings = keyToSetBindings.get(key);
      //noinspection Java8MapApi Supporting old Android API levels.
      if (setBindings == null) {
        setBindings = new SetBindings();
        keyToSetBindings.put(key, setBindings);
      }
      setBindings.elementsBindings.add(elementsBinding);

      return this;
    }

    /**
     * Create an empty map binding specified by {@code key} if it does not already exist.
     *
     * @param key The key defining the set. The raw class of the {@linkplain Key#type() type} must
     *     be {@link Map Map.class}.
     */
    Builder createMapBinding(Key key) {
      if (key == null) throw new NullPointerException("key == null");
      if (Types.getRawType(key.type()) != Map.class) {
        throw new IllegalArgumentException("key.type() must be Map");
      }

      if (!keyToMapBindings.containsKey(key)) {
        keyToMapBindings.put(key, new LinkedHashMap<>());
      }

      return this;
    }

    /**
     * Adds a new entry into the map specified by {@code key}.
     *
     * @param key The key defining the map in which this entry will be added. The raw class of the
     *     {@linkplain Key#type() type} must be {@link Map Map.class}.
     * @param entryKey The key of the new map entry. The argument must be an instance of the {@code
     *     K} type parameter of {@link Map} specified {@linkplain Key#type() in the
     *     <code>key</code>}.
     * @param entryValueBinding The value binding of the new map entry. The instance produced by
     *     this binding must be an instance of the {@code V} type parameter of {@link Map} specified
     *     {@linkplain Key#type()} in the <code>key</code>}.
     */
    Builder addBindingIntoMap(Key key, Object entryKey, Binding entryValueBinding) {
      if (key == null) throw new NullPointerException("key == null");
      if (entryKey == null) throw new NullPointerException("entryKey == null");
      if (entryValueBinding == null) throw new NullPointerException("entryValueBinding == null");
      if (Types.getRawType(key.type()) != Map.class) {
        throw new IllegalArgumentException("key.type() must be Map");
      }

      Map<Object, Binding> mapBindings = keyToMapBindings.get(key);
      //noinspection Java8MapApi Supporting old Android API levels.
      if (mapBindings == null) {
        mapBindings = new LinkedHashMap<>();
        keyToMapBindings.put(key, mapBindings);
      }
      Binding replaced = mapBindings.put(entryKey, entryValueBinding);
      if (replaced != null) {
        throw new IllegalStateException(); // TODO duplicate keys
      }

      return this;
    }

    Builder addInstance(Key key, @Nullable Object instance) {
      return addBinding(key, new LinkedInstanceBinding<>(instance));
    }

    Scope build() {
      ConcurrentHashMap<Key, Binding> allBindings = new ConcurrentHashMap<>(keyToBinding);

      // Coalesce all of the set contribution bindings for each key into a single set binding.
      for (Map.Entry<Key, SetBindings> entry : keyToSetBindings.entrySet()) {
        Key key = entry.getKey();

        // Take a defensive copy in case the builder is being re-used.
        SetBindings setBindings = entry.getValue();
        List<Binding> elementBindings = new ArrayList<>(setBindings.elementBindings);
        List<Binding> elementsBindings = new ArrayList<>(setBindings.elementsBindings);

        Binding replaced =
            allBindings.put(key, new UnlinkedSetBinding(elementBindings, elementsBindings));
        if (replaced != null) {
          throw new IllegalStateException(); // TODO implicit set binding duplicates explicit one.
        }
      }

      // Coalesce all of the bindings for each key into a single map binding.
      for (Map.Entry<Key, Map<Object, Binding>> entry : keyToMapBindings.entrySet()) {
        Key mapOfValueKey = entry.getKey();

        // Take a defensive copy in case the builder is being re-used.
        Map<Object, Binding> entryBindings = new LinkedHashMap<>(entry.getValue());

        ParameterizedType mapType = (ParameterizedType) mapOfValueKey.type();
        Type mapKeyType = mapType.getActualTypeArguments()[0];
        Type mapValueType = mapType.getActualTypeArguments()[1];

        Key mapOfProviderKey =
            Key.of(
                mapOfValueKey.qualifier(),
                new ParameterizedTypeImpl(
                    null,
                    Map.class,
                    mapKeyType,
                    new ParameterizedTypeImpl(null, Provider.class, mapValueType)));

        Binding replaced =
            allBindings.put(mapOfValueKey, new UnlinkedMapOfValueBinding(mapOfProviderKey));
        if (replaced != null) {
          throw new IllegalStateException(); // TODO implicit map binding duplicates explicit one.
        }

        replaced =
            allBindings.put(mapOfProviderKey, new UnlinkedMapOfProviderBinding(entryBindings));
        if (replaced != null) {
          throw new IllegalStateException(); // TODO implicit map binding duplicates explicit one.
        }
      }

      return new Scope(allBindings, jitLookupFactory, annotations, parent);
    }

    private static final class SetBindings {
      /** Bindings which produce a single element for the target set. */
      final List<Binding> elementBindings = new ArrayList<>();
      /** Bindings which produce a set of elements for the target set. */
      final List<Binding> elementsBindings = new ArrayList<>();

      SetBindings() {}
    }
  }
}
