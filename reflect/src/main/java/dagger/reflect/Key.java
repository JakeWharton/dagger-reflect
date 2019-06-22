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

import static dagger.reflect.Reflection.boxIfNecessary;
import static dagger.reflect.TypeUtil.canonicalize;

import com.google.auto.value.AutoValue;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import org.jetbrains.annotations.Nullable;

@AutoValue
abstract class Key {
  static Key of(@Nullable Annotation qualifier, Type type) {
    if (containsTypeVariable(type)) {
      throw new IllegalArgumentException("cannot contain type variable " + type);
    }
    return new AutoValue_Key(qualifier, canonicalize(boxIfNecessary(type)));
  }

  abstract @Nullable Annotation qualifier();

  abstract Type type();

  private static boolean containsTypeVariable(Type type) {
    if (type instanceof TypeVariable) {
      return true;
    }
    if (type instanceof ParameterizedType) {
      Type[] actualTypeArguments = ((ParameterizedType) type).getActualTypeArguments();
      for (Type arg : actualTypeArguments) {
        if (containsTypeVariable(arg)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public final String toString() {
    Annotation qualifier = qualifier();
    String type = getTypeName(type());
    return qualifier != null ? qualifier.toString() + ' ' + type : type;
  }

  /** Backport of {@link Type#getTypeName()}. */
  private static String getTypeName(Type type) {
    if (type instanceof Class<?>) {
      Class<?> cls = (Class<?>) type;
      if (cls.isArray()) {
        int cardinality = 0;
        do {
          cardinality++;
          cls = cls.getComponentType();
        } while (cls.isArray());
        StringBuilder name = new StringBuilder(cls.getName());
        for (int i = 0; i < cardinality; i++) {
          name.append("[]");
        }
        return name.toString();
      }
      return cls.getName();
    }
    return type.toString();
  }
}
