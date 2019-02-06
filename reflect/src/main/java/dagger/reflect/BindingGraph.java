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

import dagger.reflect.Binding.LinkedBinding;
import dagger.reflect.Binding.UnlinkedBinding;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.inject.Provider;
import org.jetbrains.annotations.Nullable;

final class BindingGraph {
  private final ConcurrentHashMap<Key, Binding> bindings;
  private final JustInTimeProvider jitProvider;

  private BindingGraph(Map<Key, Binding> bindings, JustInTimeProvider jitProvider) {
    this.bindings = new ConcurrentHashMap<>(bindings);
    this.jitProvider = jitProvider;
  }

  Provider<?> getProvider(Key key) {
    Binding binding = findBinding(key);
    if (binding == null) {
      throw new IllegalArgumentException("No provider available for " + key);
    }

    return binding instanceof LinkedBinding<?>
        ? (LinkedBinding<?>) binding
        : performLinking(key, (UnlinkedBinding) binding, new LinkedHashMap<>());
  }

  private LinkedBinding<?> performLinking(Key key, UnlinkedBinding unlinked,
      Map<Key, Binding> chain) {
    chain.put(key, unlinked);

    Binding.LinkRequest request = unlinked.request();
    Key[] requestKeys = request.keys;
    boolean[] requestOptionals = request.optionals;

    LinkedBinding<?>[] dependencies = new LinkedBinding<?>[requestKeys.length];
    for (int i = 0; i < requestKeys.length; i++) {
      Key requestKey = requestKeys[i];
      boolean isOptional = requestOptionals[i];
      Binding dependency = findBinding(requestKey);

      LinkedBinding<?> linkedBinding;
      if (dependency instanceof LinkedBinding<?>) {
        linkedBinding = (LinkedBinding<?>) dependency;
      } else if (dependency instanceof UnlinkedBinding) {
        if (chain.containsKey(requestKey)) {
          StringBuilder builder = new StringBuilder("Dependency cycle detected!\n");
          for (Map.Entry<Key, Binding> entry : chain.entrySet()) {
            builder.append(" * Requested: ")
                .append(entry.getKey())
                .append("\n     from ")
                .append(entry.getValue())
                .append('\n');
          }
          builder.append(" * Requested: ")
              .append(requestKey)
              .append("\n     which forms a cycle.");
          throw new IllegalStateException(builder.toString());
        }
        linkedBinding = performLinking(requestKey, (UnlinkedBinding) dependency, chain);
      } else if (isOptional) {
        linkedBinding = null;
      } else {
        StringBuilder builder = new StringBuilder("Cannot locate binding for ");
        builder.append(requestKey).append('\n');
        for (Map.Entry<Key, Binding> entry : chain.entrySet()) {
          builder.append(" * Requested: ")
              .append(entry.getKey())
              .append("\n     from ")
              .append(entry.getValue())
              .append('\n');
        }
        builder.append(" * Requested: ")
            .append(requestKey)
            .append("\n     which was not found.");
        throw new IllegalStateException(builder.toString());
      }

      dependencies[i] = linkedBinding;
    }

    chain.remove(key);

    LinkedBinding<?> linked = unlinked.link(dependencies);
    if (bindings.replace(key, unlinked, linked)) {
      return linked;
    }

    // If replace() returned false we raced another thread and lost.
    LinkedBinding<?> race = (LinkedBinding<?>) bindings.get(key);
    if (race == null) throw new AssertionError();
    return race;
  }

  private @Nullable Binding findBinding(Key key) {
    Binding binding = bindings.get(key);
    if (binding != null) {
      return binding;
    }

    Binding jitBinding = jitProvider.create(key);
    if (jitBinding != null) {
      Binding replaced = bindings.putIfAbsent(key, jitBinding);
      return replaced != null
          ? replaced // We raced another thread and lost.
          : jitBinding;
    }

    return null;
  }

  static final class Builder {
    private final Map<Key, Binding> bindings = new LinkedHashMap<>();
    private JustInTimeProvider jitProvider = JustInTimeProvider.NONE;

    Builder justInTimeProvider(JustInTimeProvider jitProvider) {
      if (jitProvider == null) throw new NullPointerException("jitProvider == null");
      this.jitProvider = jitProvider;
      return this;
    }

    Builder add(Key key, Binding binding) {
      if (key == null) throw new NullPointerException("key == null");
      if (binding == null) throw new NullPointerException("binding == null");

      Binding replaced = bindings.put(key, binding);
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
    @Nullable UnlinkedBinding create(Key key);

    JustInTimeProvider NONE = key -> null;
  }
}
