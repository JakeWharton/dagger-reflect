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

import javax.inject.Provider;

interface Binding {
  abstract class UnlinkedBinding implements Binding {
    abstract LinkRequest request();
    abstract LinkedBinding<?> link(LinkedBinding<?>[] dependencies);
  }

  final class LinkRequest {
    static final LinkRequest EMPTY = new LinkRequest(new Key[0]);

    final Key[] keys;
    final boolean[] optionals;

    LinkRequest(Key[] keys) {
      this(keys, new boolean[keys.length]);
    }

    LinkRequest(Key[] keys, boolean[] optionals) {
      this.keys = keys;
      this.optionals = optionals;
    }
  }

  abstract class LinkedBinding<T> implements Binding, Provider<T> {
    @Override public abstract String toString();
  }
}
