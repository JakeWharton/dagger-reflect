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
import static dagger.reflect.Reflection.hasAnnotation;
import static dagger.reflect.Reflection.newProxy;
import static dagger.reflect.Reflection.requireAnnotation;
import static dagger.reflect.Reflection.requireEnclosingClass;

final class ComponentFactoryInvocationHandler implements InvocationHandler {
  static <F> F forComponentFactory(Class<F> factoryClass) {
    requireAnnotation(factoryClass, Component.Factory.class);

    Class<?> componentClass = requireEnclosingClass(factoryClass);
    if ((componentClass.getModifiers() & Modifier.PUBLIC) == 0) {
      // Instances of proxies cannot create another proxy instance where the second interface is
      // not public. This prevents proxies of builders from creating proxies of the component.
      throw new IllegalArgumentException("Component interface "
          + componentClass.getCanonicalName()
          + " must be public in order to be reflectively created");
    }

    ComponentScopeBuilder scopeBuilder =
        ComponentScopeBuilder.buildComponent(componentClass);
    return newProxy(factoryClass,
        new ComponentFactoryInvocationHandler(componentClass, scopeBuilder));
  }

  static <F> F forSubcomponentFactory(Class<F> factoryClass, Scope scope) {
    requireAnnotation(factoryClass, Subcomponent.Factory.class);

    Class<?> componentClass = requireEnclosingClass(factoryClass);
    if ((componentClass.getModifiers() & Modifier.PUBLIC) == 0) {
      // Instances of proxies cannot create another proxy instance where the second interface is
      // not public. This prevents proxies of builders from creating proxies of the component.
      throw new IllegalArgumentException("Component interface "
          + componentClass.getCanonicalName()
          + " must be public in order to be reflectively created");
    }

    ComponentScopeBuilder scopeBuilder =
        ComponentScopeBuilder.buildSubcomponent(componentClass, scope);
    return newProxy(factoryClass,
        new ComponentFactoryInvocationHandler(componentClass, scopeBuilder));
  }

  private final Class<?> componentClass;
  private final ComponentScopeBuilder scopeBuilder;

  private ComponentFactoryInvocationHandler(Class<?> componentClass,
      ComponentScopeBuilder scopeBuilder) {
    this.componentClass = componentClass;
    this.scopeBuilder = scopeBuilder;
  }

  @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (method.getDeclaringClass() == Object.class) {
      return method.invoke(proxy, args);
    }

    Class<?> returnType = method.getReturnType();
    if (!returnType.isAssignableFrom(componentClass)) {
      throw new IllegalStateException(); // TODO must be assignable
    }

    Type[] parameterTypes = method.getGenericParameterTypes();
    Annotation[][] parameterAnnotations = method.getParameterAnnotations();
    for (int i = 0; i < parameterTypes.length; i++) {
      Type parameterType = parameterTypes[i];
      Object argument = args[i];

      if (hasAnnotation(parameterAnnotations[i], BindsInstance.class)) {
        Annotation qualifier = findQualifier(parameterAnnotations[i]);
        scopeBuilder.putBoundInstance(Key.of(qualifier, parameterType), argument);
      } else if (parameterType instanceof Class<?>) {
        Class<?> parameterClass = (Class<?>) parameterType;
        if (argument == null) {
          throw new NullPointerException(
              "@Component.Factory parameter " + parameterClass.getName() + " was null");
        }
        if (parameterClass.getAnnotation(Module.class) != null) {
          try {
            scopeBuilder.setModule(parameterClass, argument);
          } catch (IllegalArgumentException e) {
            throw new IllegalStateException("@Component.Factory has a parameter for module + "
                + parameterClass.getName()
                + " that isn't required", e);
          }
        } else {
          try {
            scopeBuilder.setDependency(parameterClass, argument);
          } catch (IllegalArgumentException e) {
            throw new IllegalStateException("@Component.Factory has a parameter for dependency "
                + parameterClass.getName()
                + " that isn't required", e);
          }
        }
      } else {
        throw new IllegalStateException(parameterType.toString()); // TODO unknown argument type
      }
    }

    return ComponentInvocationHandler.create(componentClass, scopeBuilder.build());
  }
}
