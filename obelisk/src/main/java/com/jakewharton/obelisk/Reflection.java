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
package com.jakewharton.obelisk;

import java.lang.reflect.Type;

final class Reflection {
  static Type boxIfNecessary(Type type) {
    if (type instanceof Class<?>) {
      Class<?> cls = (Class<?>) type;
      if (cls.isPrimitive()) {
        if (cls == boolean.class) return Boolean.class;
        if (cls == byte.class) return Byte.class;
        if (cls == char.class) return Character.class;
        if (cls == double.class) return Double.class;
        if (cls == float.class) return Float.class;
        if (cls == int.class) return Integer.class;
        if (cls == long.class) return Long.class;
        if (cls == short.class) return Short.class;
        if (cls == void.class) return Void.class;
        throw new AssertionError("Unknown primitive type: " + cls);
      }
    }
    return type;
  }

  private Reflection() {}
}
