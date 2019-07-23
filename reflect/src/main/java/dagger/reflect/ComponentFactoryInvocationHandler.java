package dagger.reflect;

import static dagger.reflect.Reflection.findQualifier;
import static dagger.reflect.Reflection.hasAnnotation;
import static dagger.reflect.Reflection.newProxy;
import static dagger.reflect.Reflection.requireAnnotation;
import static dagger.reflect.Reflection.requireEnclosingClass;

import dagger.BindsInstance;
import dagger.Component;
import dagger.Module;
import dagger.Subcomponent;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import javax.inject.Provider;

final class ComponentFactoryInvocationHandler implements InvocationHandler {
  static <F> F forComponentFactory(Class<F> factoryClass) {
    requireAnnotation(factoryClass, Component.Factory.class);

    Class<?> componentClass = requireEnclosingClass(factoryClass);
    if (!Modifier.isPublic(componentClass.getModifiers())) {
      // Instances of proxies cannot create another proxy instance where the second interface is
      // not public. This prevents proxies of builders from creating proxies of the component.
      throw new IllegalArgumentException(
          "Component interface "
              + componentClass.getCanonicalName()
              + " must be public in order to be reflectively created");
    }

    return newProxy(
        factoryClass,
        new ComponentFactoryInvocationHandler(
            componentClass, () -> ComponentScopeBuilder.buildComponent(componentClass)));
  }

  static <F> F forSubcomponentFactory(Class<F> factoryClass, Scope scope) {
    requireAnnotation(factoryClass, Subcomponent.Factory.class);

    Class<?> componentClass = requireEnclosingClass(factoryClass);
    if (!Modifier.isPublic(componentClass.getModifiers())) {
      // Instances of proxies cannot create another proxy instance where the second interface is
      // not public. This prevents proxies of builders from creating proxies of the component.
      throw new IllegalArgumentException(
          "Component interface "
              + componentClass.getCanonicalName()
              + " must be public in order to be reflectively created");
    }

    return newProxy(
        factoryClass,
        new ComponentFactoryInvocationHandler(
            componentClass, () -> ComponentScopeBuilder.buildSubcomponent(componentClass, scope)));
  }

  private final Class<?> componentClass;
  private final Provider<ComponentScopeBuilder> componentScopeBuilderProvider;

  private ComponentFactoryInvocationHandler(
      Class<?> componentClass, Provider<ComponentScopeBuilder> componentScopeBuilderProvider) {
    this.componentClass = componentClass;
    this.componentScopeBuilderProvider = componentScopeBuilderProvider;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (method.getDeclaringClass() == Object.class) {
      return method.invoke(proxy, args);
    }

    Class<?> returnType = method.getReturnType();
    if (!returnType.isAssignableFrom(componentClass)) {
      throw new IllegalStateException(); // TODO must be assignable
    }

    ComponentScopeBuilder componentScopeBuilder = componentScopeBuilderProvider.get();
    parseFactoryMethod(method, args, componentScopeBuilder);
    return ComponentInvocationHandler.create(componentClass, componentScopeBuilder.get());
  }

  static void parseFactoryMethod(Method method, Object[] args, ComponentScopeBuilder scopeBuilder) {
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
            throw new IllegalStateException(
                "@Component.Factory has a parameter for module + "
                    + parameterClass.getName()
                    + " that isn't required",
                e);
          }
        } else {
          try {
            scopeBuilder.setDependency(parameterClass, argument);
          } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                "@Component.Factory has a parameter for dependency "
                    + parameterClass.getName()
                    + " that isn't required",
                e);
          }
        }
      } else {
        throw new IllegalStateException(parameterType.toString()); // TODO unknown argument type
      }
    }
  }
}
