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
import dagger.Component;
import dagger.Module;
import dagger.Subcomponent;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

import static dagger.reflect.Reflection.findQualifier;
import static dagger.reflect.Reflection.newProxy;

final class ComponentBuilderInvocationHandler implements InvocationHandler {
  static <B> B forComponentBuilder(Class<B> builderClass) {
    if (builderClass.getAnnotation(Component.Builder.class) == null) {
      throw new IllegalArgumentException(
          builderClass.getCanonicalName() + " lacks @Component.Builder annotation");
    }

    Class<?> componentClass = builderClass.getEnclosingClass();
    if (componentClass == null) {
      throw new IllegalArgumentException(builderClass.getCanonicalName()
          + " is not a nested type inside of a component interface.");
    }
    if ((componentClass.getModifiers() & Modifier.PUBLIC) == 0) {
      // Instances of proxies cannot create another proxy instance where the second interface is
      // not public. This prevents proxies of builders from creating proxies of the component.
      throw new IllegalArgumentException("Component interface "
          + componentClass.getCanonicalName()
          + " must be public in order to be reflectively created");
    }

    ComponentScopeBuilder scopeBuilder =
        ComponentScopeBuilder.buildComponent(componentClass);
    return newProxy(builderClass,
        new ComponentBuilderInvocationHandler(componentClass, builderClass, scopeBuilder));
  }

  static <B> B forSubcomponentBuilder(Class<B> builderClass, Scope parent) {
    if (builderClass.getAnnotation(Subcomponent.Builder.class) == null) {
      throw new IllegalArgumentException(
          builderClass.getCanonicalName() + " lacks @Subcomponent.Builder annotation");
    }

    Class<?> subcomponentClass = builderClass.getEnclosingClass();
    if (subcomponentClass == null) {
      throw new IllegalArgumentException(builderClass.getCanonicalName()
          + " is not a nested type inside of a subcomponent interface.");
    }
    if ((subcomponentClass.getModifiers() & Modifier.PUBLIC) == 0) {
      // Instances of proxies cannot create another proxy instance where the second interface is
      // not public. This prevents proxies of builders from creating proxies of the component.
      throw new IllegalArgumentException("Subcomponent interface "
          + subcomponentClass.getCanonicalName()
          + " must be public in order to be reflectively created");
    }

    ComponentScopeBuilder scopeBuilder =
        ComponentScopeBuilder.buildSubcomponent(subcomponentClass, parent);
    return newProxy(builderClass,
        new ComponentBuilderInvocationHandler(subcomponentClass, builderClass, scopeBuilder));
  }

  private final Class<?> componentClass;
  private final Class<?> builderClass;
  private final ComponentScopeBuilder scopeBuilder;

  private ComponentBuilderInvocationHandler(Class<?> componentClass, Class<?> builderClass,
      ComponentScopeBuilder scopeBuilder) {
    this.componentClass = componentClass;
    this.builderClass = builderClass;
    this.scopeBuilder = scopeBuilder;
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
      return ComponentInvocationHandler.create(componentClass, scopeBuilder.build());
    }

    // TODO these are allowed to be void or a supertype
    if (returnType.equals(builderClass)) {
      if (parameterTypes.length != 1) {
        throw new IllegalStateException(); // TODO must be single arg
      }
      Object argument = args[0];

      if (method.getAnnotation(BindsInstance.class) != null) {
        Key key = Key.of(findQualifier(parameterAnnotations[0]), parameterTypes[0]);
        // TODO most nullable annotations don't have runtime retention. so maybe just always allow?
        //if (argument == null && !hasNullable(parameterAnnotations[0])) {
        //  throw new NullPointerException(); // TODO message
        //}
        scopeBuilder.putBoundInstance(key, argument);
      } else {
        Type parameterType = parameterTypes[0];
        if (parameterType instanceof Class<?>) {
          Class<?> parameterClass = (Class<?>) parameterType;
          if (argument == null) {
            throw new NullPointerException(
                "@Component.Builder parameter " + parameterClass.getName() + " was null");
          }
          if (parameterClass.getAnnotation(Module.class) != null) {
            try {
              scopeBuilder.setModule(parameterClass, argument);
            } catch (IllegalArgumentException e) {
              throw new IllegalStateException(
                  "@Component.Builder has setters for modules that aren't required: "
                      + method.getDeclaringClass().getName() + '.' + method.getName(), e);
            }
          } else {
            try {
              scopeBuilder.setDependency(parameterClass, argument);
            } catch (IllegalArgumentException e) {
              throw new IllegalStateException(
                  "@Component.Builder has setters for dependencies that aren't required: "
                      + method.getDeclaringClass().getName() + '.' + method.getName(), e);
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
