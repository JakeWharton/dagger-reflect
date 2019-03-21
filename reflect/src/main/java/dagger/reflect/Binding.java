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

import org.jetbrains.annotations.Nullable;

interface Binding {
  /**
   * Resolve any dependencies from {@code linker} which are needed to create this binding's
   * instances.
   * <p>
   * Despite receiving a {@link Scope}, DO NOT use this to resolve any bindings. This instance is
   * only for creating child scopes as part of this binding's created instances.
   */
  // TODO if we ever make this public API we may need to enforce Scope isn't used to get bindings.
  LinkedBinding<?> link(Linker linker, Scope scope);
  Binding asScoped();
  @Override String toString();

  abstract class UnlinkedBinding implements Binding {
    @Override public final Binding asScoped() {
      return new UnlinkedScopedBinding(this);
    }
  }

  abstract class LinkedBinding<T> implements Binding {
    abstract @Nullable T get();

    @Override public final LinkedBinding<?> link(Linker linker, Scope scope) {
      return this;
    }

    @Override public final Binding asScoped() {
      return new LinkedScopedBinding<>(this);
    }
  }
}
