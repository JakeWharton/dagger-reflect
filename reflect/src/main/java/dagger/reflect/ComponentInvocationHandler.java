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
import static dagger.reflect.Reflection.newProxy;

import dagger.MembersInjector;
import dagger.Subcomponent;
import dagger.reflect.Binding.LinkedBinding;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.Nullable;

final class ComponentInvocationHandler implements InvocationHandler {
  static <C> C forComponent(Class<C> cls) {
    Scope.Builder scopeBuilder = ComponentScopeBuilder.buildComponent(cls).get();
    return create(cls, scopeBuilder);
  }

  static <C> C create(Class<C> cls, Scope.Builder scopeBuilder) {
    Key componentKey = Key.of(null, cls);
    LinkedLateInstanceBinding<C> componentBinding = new LinkedLateInstanceBinding<>();
    scopeBuilder.addBinding(componentKey, componentBinding);

    Scope scope = scopeBuilder.build();
    C instance = newProxy(cls, new ComponentInvocationHandler(scope));
    componentBinding.setValue(instance);

    return instance;
  }

  private final Scope scope;
  private final ConcurrentHashMap<Method, MethodInvocationHandler> handlers =
      new ConcurrentHashMap<>();

  private ComponentInvocationHandler(Scope scope) {
    this.scope = scope;
  }

  @Override
  public @Nullable Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (method.getDeclaringClass() == Object.class) {
      return method.invoke(this, args);
    }

    MethodInvocationHandler handler = handlers.get(method);
    if (handler == null) {
      handler = createMethodInvocationHandler(method, scope);
      MethodInvocationHandler replaced = handlers.putIfAbsent(method, handler);
      if (replaced != null) {
        handler = replaced;
      }
    }
    return handler.invoke(args);
  }

  private static ComponentInvocationHandler.MethodInvocationHandler createMethodInvocationHandler(
      Method method, Scope scope) {
    Type returnType = method.getGenericReturnType();
    Class<?>[] parameterTypes = method.getParameterTypes();

    if (returnType instanceof Class<?>) {
      Class<?> returnClass = (Class<?>) returnType;
      if (returnClass.getAnnotation(Subcomponent.class) != null) {
        return new SubcomponentMethodInvocationHandler(method, returnClass, scope);
      }
      if (returnClass.getAnnotation(Subcomponent.Builder.class) != null) {
        if (parameterTypes.length != 0) {
          throw new IllegalStateException(method.toString()); // TODO
        }
        return new SubcomponentBuilderMethodInvocationHandler(returnClass, scope);
      }
      if (returnClass.getAnnotation(Subcomponent.Factory.class) != null) {
        if (parameterTypes.length != 0) {
          throw new IllegalStateException(method.toString()); // TODO
        }
        return new SubcomponentFactoryMethodInvocationHandler(returnClass, scope);
      }
    }

    if (parameterTypes.length == 0) {
      Key key = Key.of(findQualifier(method.getDeclaredAnnotations()), returnType);
      LinkedBinding<?> binding = scope.getBinding(key);
      return new ProvisionMethodInvocationHandler(binding);
    }

    if (parameterTypes.length == 1) {
      boolean returnInstance;
      if (returnType == void.class) {
        returnInstance = false;
      } else if (method.getReturnType().equals(parameterTypes[0])) {
        returnInstance = true;
      } else {
        throw new IllegalStateException(
            "Members injection methods may only return the injected type or void: "
                + method.getDeclaringClass().getName()
                + '.'
                + method.getName());
      }

      // RedundantCast: see https://youtrack.jetbrains.com/issue/IDEA-206560
      @SuppressWarnings({"unchecked", "RedundantCast"})
      MembersInjector<Object> injector =
          (MembersInjector<Object>) ReflectiveMembersInjector.create(parameterTypes[0], scope);
      return new MembersInjectorMethodInvocationHandler(injector, returnInstance);
    }

    throw new IllegalStateException(method.toString()); // TODO unsupported method shape
  }

  private interface MethodInvocationHandler {
    @Nullable
    Object invoke(Object[] args);
  }

  private static final class ProvisionMethodInvocationHandler implements MethodInvocationHandler {
    private final LinkedBinding<?> binding;

    ProvisionMethodInvocationHandler(LinkedBinding<?> binding) {
      this.binding = binding;
    }

    @Override
    public @Nullable Object invoke(Object[] args) {
      return binding.get();
    }
  }

  private static final class MembersInjectorMethodInvocationHandler
      implements MethodInvocationHandler {
    private final MembersInjector<Object> membersInjector;
    private final boolean returnInstance;

    MembersInjectorMethodInvocationHandler(
        MembersInjector<Object> membersInjector, boolean returnInstance) {
      this.membersInjector = membersInjector;
      this.returnInstance = returnInstance;
    }

    @Override
    public @Nullable Object invoke(Object[] args) {
      Object instance = args[0];
      membersInjector.injectMembers(instance);
      return returnInstance ? instance : null;
    }
  }

  private static final class SubcomponentMethodInvocationHandler
      implements MethodInvocationHandler {
    private final Method method;
    private final Class<?> cls;
    private final Scope scope;

    SubcomponentMethodInvocationHandler(Method method, Class<?> cls, Scope scope) {
      this.method = method;
      this.cls = cls;
      this.scope = scope;
    }

    @Override
    public Object invoke(Object[] args) {
      ComponentScopeBuilder scopeBuilder = ComponentScopeBuilder.buildSubcomponent(cls, scope);
      ComponentFactoryInvocationHandler.parseFactoryMethod(method, args, scopeBuilder);
      return create(cls, scopeBuilder.get());
    }
  }

  private static final class SubcomponentBuilderMethodInvocationHandler
      implements MethodInvocationHandler {
    private final Class<?> cls;
    private final Scope scope;

    SubcomponentBuilderMethodInvocationHandler(Class<?> cls, Scope scope) {
      this.cls = cls;
      this.scope = scope;
    }

    @Override
    public Object invoke(Object[] args) {
      return ComponentBuilderInvocationHandler.forSubcomponentBuilder(cls, scope);
    }
  }

  private static final class SubcomponentFactoryMethodInvocationHandler
      implements MethodInvocationHandler {
    private final Class<?> cls;
    private final Scope scope;

    SubcomponentFactoryMethodInvocationHandler(Class<?> cls, Scope scope) {
      this.cls = cls;
      this.scope = scope;
    }

    @Override
    public Object invoke(Object[] args) {
      return ComponentFactoryInvocationHandler.forSubcomponentFactory(cls, scope);
    }
  }
}
