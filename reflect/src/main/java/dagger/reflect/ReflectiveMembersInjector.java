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

import dagger.MembersInjector;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Provider;

import static dagger.reflect.Reflection.findQualifier;
import static dagger.reflect.Reflection.tryInvoke;
import static dagger.reflect.Reflection.trySet;

final class ReflectiveMembersInjector<T> implements MembersInjector<T> {
  static <T> MembersInjector<T> create(Class<T> cls, BindingGraph graph) {
    Deque<ClassInjector<T>> classInjectors = new ArrayDeque<>();
    Class<?> target = cls;
    while (target != Object.class && target != null) {
      Map<Field, Provider<?>> fieldProviders = new LinkedHashMap<>();
      for (Field field : target.getDeclaredFields()) {
        if (field.getAnnotation(Inject.class) == null) {
          continue;
        }
        if ((field.getModifiers() & Modifier.PRIVATE) != 0) {
          throw new IllegalArgumentException("Dagger does not support injection into private fields: "
                  + target.getCanonicalName() + "." + field.getName());
        }
        if ((field.getModifiers() & Modifier.STATIC) != 0) {
          throw new IllegalArgumentException("Dagger does not support injection into static fields: "
                  + target.getCanonicalName() + "." + field.getName());
        }

        Key key = Key.of(findQualifier(field.getDeclaredAnnotations()), field.getGenericType());
        Provider<?> provider = graph.getProvider(key);

        fieldProviders.put(field, provider);
      }

      Map<Method, Provider<?>[]> methodProviders = new LinkedHashMap<>();
      for (Method method : target.getDeclaredMethods()) {
        if (method.getAnnotation(Inject.class) == null) {
          continue;
        }
        if ((method.getModifiers() & Modifier.PRIVATE) != 0) {
          throw new IllegalArgumentException("Dagger does not support injection into private methods: "
                  + target.getCanonicalName() + "." + method.getName() + "()");
        }
        if ((method.getModifiers() & Modifier.STATIC) != 0) {
          throw new IllegalArgumentException("Dagger does not support injection into static methods: "
                  + target.getCanonicalName() + "." + method.getName() + "()");
        }
        if ((method.getModifiers() & Modifier.ABSTRACT) != 0) {
          throw new IllegalArgumentException("Methods with @Inject may not be abstract: "
              + target.getCanonicalName() + "." + method.getName() + "()");
        }

        Type[] parameterTypes = method.getGenericParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Provider<?>[] providers = new Provider<?>[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
          Key key = Key.of(findQualifier(parameterAnnotations[i]), parameterTypes[i]);
          providers[i] = graph.getProvider(key);
        }

        methodProviders.put(method, providers);
      }

      if (!fieldProviders.isEmpty() || !methodProviders.isEmpty()) {
        // Per JSR 330, fields and methods in superclasses are injected before those in subclasses.
        // We are traversing upward in the class hierarchy so each injector is prepended to the
        // collection to ensure regular iteration will honor this contract.
        classInjectors.addFirst(new ClassInjector<>(fieldProviders, methodProviders));
      }

      target = target.getSuperclass();
    }

    return new ReflectiveMembersInjector<>(classInjectors);
  }

  private final Iterable<ClassInjector<T>> classInjectors;

  private ReflectiveMembersInjector(Iterable<ClassInjector<T>> classInjectors) {
    this.classInjectors = classInjectors;
  }

  @Override public void injectMembers(T instance) {
    for (ClassInjector<T> classInjector : classInjectors) {
      classInjector.injectMembers(instance);
    }
  }

  private static final class ClassInjector<T> implements MembersInjector<T> {
    final Map<Field, Provider<?>> fieldProviders;
    final Map<Method, Provider<?>[]> methodProviders;

    ClassInjector(
        Map<Field, Provider<?>> fieldProviders,
        Map<Method, Provider<?>[]> methodProviders) {
      this.fieldProviders = fieldProviders;
      this.methodProviders = methodProviders;
    }

    @Override public void injectMembers(T instance) {
      // Per JSR 330, fields are injected before methods.
      for (Map.Entry<Field, Provider<?>> fieldProvider : fieldProviders.entrySet()) {
        trySet(instance, fieldProvider.getKey(), fieldProvider.getValue().get());
      }
      for (Map.Entry<Method, Provider<?>[]> methodProvider : methodProviders.entrySet()) {
        Provider<?>[] providers = methodProvider.getValue();
        Object[] arguments = new Object[providers.length];
        for (int i = 0; i < providers.length; i++) {
          arguments[i] = providers[i].get();
        }
        tryInvoke(instance, methodProvider.getKey(), arguments);
      }
    }
  }
}
