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

import dagger.MapKey;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.inject.Qualifier;
import javax.inject.Scope;
import org.jetbrains.annotations.Nullable;

final class Reflection {
  static @Nullable Class<?> findEnclosedAnnotatedClass(Class<?> cls,
      Class<? extends Annotation> annotationClass) {
    for (Class<?> declaredClass : cls.getDeclaredClasses()) {
      if (declaredClass.getAnnotation(annotationClass) != null) {
        return declaredClass;
      }
    }
    return null;
  }

  static @Nullable Annotation findQualifier(Annotation[] annotations) {
    Annotation qualifier = null;
    for (Annotation annotation : annotations) {
      if (annotation.annotationType().getAnnotation(Qualifier.class) != null) {
        if (qualifier != null) {
          throw new IllegalArgumentException(
              "Multiple qualifier annotations: " + qualifier + " and " + annotation);
        }
        qualifier = annotation;
      }
    }
    return qualifier;
  }

  static @Nullable Annotation findScope(Annotation[] annotations) {
    Annotation scope = null;
    for (Annotation annotation : annotations) {
      if (annotation.annotationType().getAnnotation(Scope.class) != null) {
        if (scope != null) {
          throw new IllegalArgumentException(
              "Multiple scope annotations: " + scope + " and " + annotation);
        }
        scope = annotation;
      }
    }
    return scope;
  }

  static @Nullable Annotation findMapKey(Annotation[] annotations) {
    Annotation key = null;
    for (Annotation annotation : annotations) {
      if (annotation.annotationType().getAnnotation(MapKey.class) != null) {
        if (key != null) {
          throw new IllegalArgumentException(
              "Multiple key annotations: " + key + " and " + annotation);
        }
        key = annotation;
      }
    }
    return key;
  }

  static @Nullable <T extends Annotation> T findAnnotation(Annotation[] annotations,
      Class<T> annotationType) {
    for (Annotation annotation : annotations) {
      if (annotation.annotationType() == annotationType) {
        return annotationType.cast(annotation);
      }
    }
    return null;
  }

  static boolean hasAnnotation(Annotation[] annotations, Class<? extends Annotation> annotation) {
    return findAnnotation(annotations, annotation) != null;
  }

  @SuppressWarnings("StringConcatenationInLoop") // Only occurs when about to throw an exception.
  static <T extends Annotation> T requireAnnotation(Class<?> cls, Class<T> annotationClass) {
    T annotation = cls.getAnnotation(annotationClass);
    if (annotation != null) {
      return annotation;
    }

    String name = "";
    for (Class<?> a = annotationClass; a != null; a = a.getEnclosingClass()) {
      if (!name.isEmpty()) {
        name = "." + name;
      }
      name = a.getSimpleName() + name;
    }
    throw new IllegalArgumentException(cls.getCanonicalName() + " lacks @" + name + " annotation");
  }

  static Class<?> requireEnclosingClass(Class<?> cls) {
    Class<?> enclosingClass = cls.getEnclosingClass();
    if (enclosingClass != null) {
      return enclosingClass;
    }
    throw new IllegalArgumentException(cls.getCanonicalName() + " must be nested in another type");
  }

  static void trySet(@Nullable Object instance, Field field, @Nullable Object value) {
    if ((field.getModifiers() & Modifier.PUBLIC) == 0) {
      field.setAccessible(true);
    }
    try {
      field.set(instance, value);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Unable to set " + value + " to " + field + " on " + instance, e);
    }
  }

  static @Nullable Object tryInvoke(@Nullable Object instance, Method method, Object... arguments) {
    if ((method.getModifiers() & Modifier.PUBLIC) == 0) {
      method.setAccessible(true);
    }
    try {
      return method.invoke(instance, arguments);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Unable to invoke " + method + " on " + instance, e);
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      if (cause instanceof RuntimeException) throw (RuntimeException) cause;
      if (cause instanceof Error) throw (Error) cause;
      throw new RuntimeException("Unable to invoke " + method + " on " + instance, cause);
    }
  }

  static <T> T tryInstantiate(Constructor<T> constructor, Object... arguments) {
    if ((constructor.getModifiers() & Modifier.PUBLIC) == 0) {
      constructor.setAccessible(true);
    }
    try {
      return constructor.newInstance(arguments);
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException("Unable to invoke " + constructor, e);
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      if (cause instanceof RuntimeException) throw (RuntimeException) cause;
      if (cause instanceof Error) throw (Error) cause;
      throw new RuntimeException("Unable to invoke " + constructor, cause);
    }
  }

  /**
   * Try to create an instance of {@code cls} using a default constructor. Returns null if no
   * default constructor found.
   */
  @SuppressWarnings("unchecked") // Casts implied by cls generic type.
  static <T> @Nullable T maybeInstantiate(Class<T> cls) {
    for (Constructor<?> constructor : cls.getDeclaredConstructors()) {
      if (constructor.getParameterTypes().length == 0) {
        return (T) tryInstantiate(constructor);
      }
    }
    return null;
  }

  static Set<Class<?>> getDistinctTypeHierarchy(Class<?> target) {
    Set<Class<?>> types = new LinkedHashSet<>();
    do {
      types.add(target);
      Collections.addAll(types, target.getInterfaces());
      target = target.getSuperclass();
    } while (target != null && target != Object.class);
    return types;
  }

  static <T> T newProxy(Class<T> cls, InvocationHandler handler) {
    if (!cls.isInterface()) {
      throw new IllegalArgumentException(cls.getCanonicalName()
          + " is not an interface. Only interfaces are supported.");
    }
    return cls.cast(Proxy.newProxyInstance(cls.getClassLoader(), new Class<?>[] { cls }, handler));
  }

  private Reflection() {
    throw new AssertionError();
  }
}
