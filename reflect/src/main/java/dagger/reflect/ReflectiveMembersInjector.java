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

import static dagger.reflect.Reflection.findQualifier;
import static dagger.reflect.Reflection.tryInvoke;
import static dagger.reflect.Reflection.trySet;

import dagger.MembersInjector;
import dagger.reflect.Binding.LinkedBinding;
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

final class ReflectiveMembersInjector<T> implements MembersInjector<T> {
  static <T> MembersInjector<T> create(Class<T> cls, Scope scope) {
    Deque<ClassInjector<T>> classInjectors = new ArrayDeque<>();
    Class<?> target = cls;
    while (target != Object.class && target != null) {
      Map<Field, LinkedBinding<?>> fieldBindings = new LinkedHashMap<>();
      for (Field field : target.getDeclaredFields()) {
        if (field.getAnnotation(Inject.class) == null) {
          continue;
        }
        if (Modifier.isPrivate(field.getModifiers())) {
          throw new IllegalArgumentException(
              "Dagger does not support injection into private fields: "
                  + target.getCanonicalName()
                  + "."
                  + field.getName());
        }
        if (Modifier.isStatic(field.getModifiers())) {
          throw new IllegalArgumentException(
              "Dagger does not support injection into static fields: "
                  + target.getCanonicalName()
                  + "."
                  + field.getName());
        }

        Key key = Key.of(findQualifier(field.getDeclaredAnnotations()), field.getGenericType());
        LinkedBinding<?> binding = scope.getBinding(key);

        fieldBindings.put(field, binding);
      }

      Map<Method, LinkedBinding<?>[]> methodBindings = new LinkedHashMap<>();
      for (Method method : target.getDeclaredMethods()) {
        if (method.getAnnotation(Inject.class) == null) {
          continue;
        }
        if (Modifier.isPrivate(method.getModifiers())) {
          throw new IllegalArgumentException(
              "Dagger does not support injection into private methods: "
                  + target.getCanonicalName()
                  + "."
                  + method.getName()
                  + "()");
        }
        if (Modifier.isStatic(method.getModifiers())) {
          throw new IllegalArgumentException(
              "Dagger does not support injection into static methods: "
                  + target.getCanonicalName()
                  + "."
                  + method.getName()
                  + "()");
        }
        if (Modifier.isAbstract(method.getModifiers())) {
          throw new IllegalArgumentException(
              "Methods with @Inject may not be abstract: "
                  + target.getCanonicalName()
                  + "."
                  + method.getName()
                  + "()");
        }

        Type[] parameterTypes = method.getGenericParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        LinkedBinding<?>[] bindings = new LinkedBinding<?>[parameterTypes.length];
        for (int i = 0; i < parameterTypes.length; i++) {
          Key key = Key.of(findQualifier(parameterAnnotations[i]), parameterTypes[i]);
          bindings[i] = scope.getBinding(key);
        }

        methodBindings.put(method, bindings);
      }

      if (!fieldBindings.isEmpty() || !methodBindings.isEmpty()) {
        // Per JSR 330, fields and methods in superclasses are injected before those in subclasses.
        // We are traversing upward in the class hierarchy so each injector is prepended to the
        // collection to ensure regular iteration will honor this contract.
        classInjectors.addFirst(new ClassInjector<>(fieldBindings, methodBindings));
      }

      target = target.getSuperclass();
    }

    return new ReflectiveMembersInjector<>(classInjectors);
  }

  private final Iterable<ClassInjector<T>> classInjectors;

  private ReflectiveMembersInjector(Iterable<ClassInjector<T>> classInjectors) {
    this.classInjectors = classInjectors;
  }

  @Override
  public void injectMembers(T instance) {
    for (ClassInjector<T> classInjector : classInjectors) {
      classInjector.injectMembers(instance);
    }
  }

  private static final class ClassInjector<T> implements MembersInjector<T> {
    final Map<Field, LinkedBinding<?>> fieldBindings;
    final Map<Method, LinkedBinding<?>[]> methodBindings;

    ClassInjector(
        Map<Field, LinkedBinding<?>> fieldBindings,
        Map<Method, LinkedBinding<?>[]> methodBindings) {
      this.fieldBindings = fieldBindings;
      this.methodBindings = methodBindings;
    }

    @Override
    public void injectMembers(T instance) {
      // Per JSR 330, fields are injected before methods.
      for (Map.Entry<Field, LinkedBinding<?>> fieldBinding : fieldBindings.entrySet()) {
        trySet(instance, fieldBinding.getKey(), fieldBinding.getValue().get());
      }
      for (Map.Entry<Method, LinkedBinding<?>[]> methodBinding : methodBindings.entrySet()) {
        LinkedBinding<?>[] bindings = methodBinding.getValue();
        Object[] arguments = new Object[bindings.length];
        for (int i = 0; i < bindings.length; i++) {
          arguments[i] = bindings[i].get();
        }
        tryInvoke(instance, methodBinding.getKey(), arguments);
      }
    }
  }
}
