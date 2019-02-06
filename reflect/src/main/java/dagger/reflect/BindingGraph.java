/*
 * Copyright (C) 2018 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dagger.reflect;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.Nullable;

final class BindingGraph {
  private final ConcurrentHashMap<Key, Binding<?>> bindings;
  private final JustInTimeProvider jitProvider;

  private BindingGraph(Map<Key, Binding<?>> bindings, JustInTimeProvider jitProvider) {
    this.bindings = new ConcurrentHashMap<>(bindings);
    this.jitProvider = jitProvider;
  }

  Binding<?> getBinding(Key key) {
    Binding<?> binding = locateBinding(key);
    return binding.isLinked()
        ? binding
        : performLinking(key, binding, new LinkedHashMap<>());
  }

  private Binding<?> performLinking(Key key, Binding<?> unlinked, Map<Key, Binding<?>> chain) {
    chain.put(key, unlinked);

    Key[] dependencyKeys = unlinked.dependencies();
    Binding<?>[] dependencies = new Binding<?>[dependencyKeys.length];

    for (int i = 0; i < dependencyKeys.length; i++) {
      Key dependencyKey = dependencyKeys[i];
      @Nullable Binding<?> dependency = locateBinding(dependencyKey);

      if (dependency == null && Types.equals(Types.getRawType(key.type()), Optional.class)) {
        continue;
      }

      if (!dependency.isLinked()) {
        if (chain.containsKey(dependencyKey)) {
          StringBuilder builder = new StringBuilder("Dependency cycle detected!\n");
          for (Map.Entry<Key, Binding<?>> entry : chain.entrySet()) {
            builder.append(" * Requested: ")
                .append(entry.getKey())
                .append("\n     from ")
                .append(entry.getValue())
                .append('\n');
          }
          builder.append(" * Requested: ")
              .append(dependencyKey)
              .append("\n     which forms a cycle.");
          throw new IllegalStateException(builder.toString());
        }

        dependency = performLinking(dependencyKey, dependency, chain);
      }

      dependencies[i] = dependency;
    }

    chain.remove(key);

    Binding<?> linked = unlinked.link(dependencies);
    if (!linked.isLinked()) {
      throw new IllegalStateException("A call to link on "
          + unlinked
          + " returned "
          + linked
          + " but it reported itself as unlinked.");
    }

    if (bindings.replace(key, unlinked, linked)) {
      return linked;
    }

    // If replace() returned false we raced another thread and lost.
    Binding<?> race = bindings.get(key);
    if (race == null) throw new AssertionError();
    return race;
  }

  @Nullable private Binding<?> locateBinding(Key key) {
    Binding<?> binding = bindings.get(key);
    if (binding != null) {
      return binding;
    }

    Binding<?> jitBinding = jitProvider.create(key);
    if (jitBinding != null) {
      Binding<?> replaced = bindings.putIfAbsent(key, jitBinding);
      return replaced != null
          ? replaced // We raced another thread and lost.
          : jitBinding;
    }
    else {
      return null;
    }
  }

  static final class Builder {
    private final Map<Key, Binding<?>> bindings = new LinkedHashMap<>();
    private JustInTimeProvider jitProvider = JustInTimeProvider.NONE;

    Builder justInTimeProvider(JustInTimeProvider jitProvider) {
      if (jitProvider == null) throw new NullPointerException("jitProvider == null");
      this.jitProvider = jitProvider;
      return this;
    }

    Builder add(Key key, Binding<?> binding) {
      if (key == null) throw new NullPointerException("key == null");
      if (binding == null) throw new NullPointerException("binding == null");

      Binding<?> replaced = bindings.put(key, binding);
      if (replaced != null) {
        throw new IllegalStateException(
            "Duplicate binding for " + key + ": " + replaced + " and " + binding);
      }
      return this;
    }

    BindingGraph build() {
      return new BindingGraph(bindings, jitProvider);
    }
  }

  interface JustInTimeProvider {
    @Nullable Binding<?> create(Key key);

    JustInTimeProvider NONE = key -> null;
  }
}
