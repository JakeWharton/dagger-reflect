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

import dagger.BindsInstance;
import dagger.Module;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static dagger.reflect.Reflection.findQualifier;

final class ComponentBuilderInvocationHandler implements InvocationHandler {
  static <T> T create(Class<?> componentClass, Class<T> builderClass, Set<Class<?>> modules,
      Set<Class<?>> dependencies) {
    if ((componentClass.getModifiers() & Modifier.PUBLIC) == 0) {
      // Instances of proxies cannot create another proxy instance where the second interface is
      // not public. This prevents proxies of builders from creating proxies of the component.
      throw new IllegalArgumentException("Component interface "
          + componentClass.getName()
          + " must be public in order to be reflectively created");
    }
    return builderClass.cast(
        Proxy.newProxyInstance(builderClass.getClassLoader(), new Class<?>[] { builderClass },
            new ComponentBuilderInvocationHandler(componentClass, builderClass, modules,
                dependencies)));
  }

  private final Class<?> componentClass;
  private final Class<?> builderClass;
  private final Map<Key, Object> boundInstances;
  private final Map<Class<?>, Object> moduleInstances;
  private final Map<Class<?>, Object> dependencyInstances;

  private ComponentBuilderInvocationHandler(Class<?> componentClass, Class<?> builderClass,
      Iterable<Class<?>> componentModules, Iterable<Class<?>> componentDependencies) {
    this.componentClass = componentClass;
    this.builderClass = builderClass;
    this.boundInstances = new LinkedHashMap<>();

    // Start with all modules bound to null. Any remaining nulls will be assumed stateless.
    moduleInstances = new LinkedHashMap<>();
    for (Class<?> componentModule : componentModules) {
      moduleInstances.put(componentModule, null);
    }

    // Start with all dependencies as null. Any remaining nulls at creation time is an error.
    dependencyInstances = new LinkedHashMap<>();
    for (Class<?> componentDependency : componentDependencies) {
      dependencyInstances.put(componentDependency, null);
    }
  }

  @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (method.getDeclaringClass() == Object.class) {
      return method.invoke(proxy, args);
    }

    Class<?> returnType = method.getReturnType();
    Type[] parameterTypes = method.getGenericParameterTypes();
    Annotation[][] parameterAnnotations = method.getParameterAnnotations();

    if (returnType.equals(componentClass)) {
      if (parameterTypes.length != 0) {
        throw new IllegalStateException(); // TODO must be no-arg
      }

      BindingGraph.Builder graphBuilder = new BindingGraph.Builder()
          .justInTimeProvider(new ReflectiveJustInTimeProvider());

      for (Map.Entry<Key, Object> entry : boundInstances.entrySet()) {
        graphBuilder.add(entry.getKey(), new Binding.Instance<>(entry.getValue()));
      }
      for (Map.Entry<Class<?>, Object> entry : moduleInstances.entrySet()) {
        ReflectiveModuleParser.parse(entry.getKey(), entry.getValue(), graphBuilder);
      }
      for (Map.Entry<Class<?>, Object> entry : dependencyInstances.entrySet()) {
        Class<?> key = entry.getKey();
        Object value = entry.getValue();
        if (value == null) {
          throw new IllegalStateException(key.getCanonicalName() + " must be set");
        }
        ReflectiveComponentParser.parse(key, value, graphBuilder);
      }

      return ComponentInvocationHandler.create(componentClass, graphBuilder.build());
    }

    // TODO these are allowed to be void or a supertype
    if (returnType.equals(builderClass)) {
      if (parameterTypes.length != 1) {
        throw new IllegalStateException(); // TODO must be single arg
      }

      if (method.getAnnotation(BindsInstance.class) != null) {
        Key key = Key.of(findQualifier(parameterAnnotations[0]), parameterTypes[0]);
        Object instance = args[0];
        // TODO most nullable annotations don't have runtime retention. so maybe just always allow?
        //if (instance == null && !hasNullable(parameterAnnotations[0])) {
        //  throw new NullPointerException(); // TODO message
        //}
        boundInstances.put(key, instance);
      } else {
        Type parameterType = parameterTypes[0];
        if (parameterType instanceof Class<?>) {
          Class<?> parameterClass = (Class<?>) parameterType;
          if (parameterClass.getAnnotation(Module.class) != null) {
            if (moduleInstances.containsKey(parameterClass)) {
              moduleInstances.put(parameterClass, args[0]);
            } else {
              throw new IllegalStateException(
                  "@Component.Builder has setters for modules or components that aren't required: "
                      + "[" + method + "]");
            }
          } else {
            if (dependencyInstances.containsKey(parameterClass)) {
              dependencyInstances.put(parameterClass, args[0]);
            } else {
              throw new IllegalStateException(
                  "@Component.Builder has setters for modules or components that aren't required: "
                      + "[" + method + "]");
            }
          }
        } else {
          throw new IllegalStateException(method.toString()); // TODO report unsupported method shape
        }
      }
      return proxy;
    }

    throw new IllegalStateException(method.toString()); // TODO report unsupported method shape
  }
}
