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
package dagger.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayDeque;
import java.util.Deque;

public final class DaggerCodegen {
  public static <C> C create(Class<C> componentClass) {
    return invokeStatic(findImplementationClass(componentClass), "create", componentClass);
  }

  public static <B> B builder(Class<B> builderClass) {
    Class<?> componentClass = builderClass.getEnclosingClass();
    if (componentClass == null) {
      throw new IllegalArgumentException(
          builderClass.getCanonicalName()
              + " is not a nested type inside of a component interface");
    }
    return invokeStatic(findImplementationClass(componentClass), "builder", builderClass);
  }

  public static <F> F factory(Class<F> factoryClass) {
    Class<?> componentClass = factoryClass.getEnclosingClass();
    if (componentClass == null) {
      throw new IllegalArgumentException(
          factoryClass.getCanonicalName()
              + " is not a nested type inside of a component interface");
    }
    return invokeStatic(findImplementationClass(componentClass), "factory", factoryClass);
  }

  private static <C> Class<? extends C> findImplementationClass(Class<C> componentClass) {
    String implementationName = deduceImplementationClassName(componentClass);
    try {
      // Dagger compiler guarantees this cast to succeed.
      @SuppressWarnings("unchecked")
      Class<? extends C> implementationClass =
          (Class<? extends C>) componentClass.getClassLoader().loadClass(implementationName);
      return implementationClass;
    } catch (ClassNotFoundException e) {
      throw new IllegalStateException(
          "Unable to find generated component implementation "
              + implementationName
              + " for component "
              + componentClass.getName(),
          e);
    }
  }

  private static <C> String deduceImplementationClassName(Class<C> componentClass) {
    Deque<Class<?>> classes = new ArrayDeque<>();
    Class<?> nesting = componentClass.getEnclosingClass();
    while (nesting != null) {
      classes.addFirst(nesting);
      nesting = nesting.getEnclosingClass();
    }

    StringBuilder daggerName = new StringBuilder();
    daggerName.append(componentClass.getPackage().getName());
    daggerName.append(".Dagger");
    for (Class<?> clazz : classes) {
      daggerName.append(clazz.getSimpleName());
      daggerName.append('_');
    }
    daggerName.append(componentClass.getSimpleName());
    return daggerName.toString();
  }

  private static <T> T invokeStatic(Class<?> target, String name, Class<T> returnType) {
    Method method;
    try {
      method = target.getMethod(name);
    } catch (NoSuchMethodException e) {
      throw new IllegalStateException("Unable to find method '" + name + "' on " + target, e);
    }
    if (!method.isAccessible()) {
      method.setAccessible(true);
    }
    Object returnValue;
    try {
      returnValue = method.invoke(null);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException("Unable to invoke method '" + name + "' on " + target, e);
    } catch (InvocationTargetException e) {
      Throwable cause = e.getCause();
      if (cause instanceof RuntimeException) throw (RuntimeException) cause;
      if (cause instanceof Error) throw (Error) cause;
      throw new RuntimeException("Exception while reflectively invoking method", e);
    }
    return returnType.cast(returnValue);
  }

  private DaggerCodegen() {
    throw new AssertionError();
  }
}
