package dagger.reflect;

import dagger.BindsInstance;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.util.LinkedHashSet;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

import static dagger.reflect.Reflection.findAnnotation;
import static dagger.reflect.Reflection.findQualifier;
import static dagger.reflect.Reflection.findScope;

final class ComponentFactoryInvocationHandler implements InvocationHandler {
  static <T> T create(Class<?> componentClass, Class<T> factoryClass, Set<Class<?>> modules,
      Set<Class<?>> dependencies, @Nullable Scope parent) {
    if (!componentClass.isInterface()) {
      throw new IllegalArgumentException(componentClass.getCanonicalName()
          + " is not an interface. Only interfaces are supported.");
    }
    if ((componentClass.getModifiers() & Modifier.PUBLIC) == 0) {
      // Instances of proxies cannot create another proxy instance where the second interface is
      // not public. This prevents proxies of builders from creating proxies of the component.
      throw new IllegalArgumentException("Component interface "
          + componentClass.getCanonicalName()
          + " must be public in order to be reflectively created");
    }
    if (!factoryClass.isInterface()) {
      throw new IllegalArgumentException(factoryClass.getCanonicalName()
          + " is not an interface. Only interface factories are supported.");
    }
    return factoryClass.cast(
        Proxy.newProxyInstance(factoryClass.getClassLoader(), new Class<?>[] { factoryClass },
            new ComponentFactoryInvocationHandler(componentClass, factoryClass, modules,
                dependencies, parent)));
  }

  private final Class<?> componentClass;
  private final Class<?> factoryClass;
  private final Set<Class<?>> componentModules;
  private final Set<Class<?>> componentDependencies;
  private final @Nullable Scope parent;

  private ComponentFactoryInvocationHandler(Class<?> componentClass, Class<?> factoryClass,
      Set<Class<?>> componentModules, Set<Class<?>> componentDependencies, @Nullable Scope parent) {
    this.componentClass = componentClass;
    this.factoryClass = factoryClass;
    this.componentModules = componentModules;
    this.componentDependencies = componentDependencies;
    this.parent = parent;
  }

  @Override public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    if (method.getDeclaringClass() == Object.class) {
      return method.invoke(proxy, args);
    }

    Class<?> returnType = method.getReturnType();
    if (!returnType.isAssignableFrom(componentClass)) {
      throw new IllegalStateException(); // TODO must be assignable
    }

    Annotation scopeAnnotation = findScope(factoryClass.getDeclaredAnnotations());
    Scope.Builder scope = new Scope.Builder(parent, scopeAnnotation)
        .justInTimeLookupFactory(new ReflectiveJustInTimeLookupFactory());

    Set<Class<?>> missingModules = new LinkedHashSet<>(componentModules);
    Set<Class<?>> missingDependencies = new LinkedHashSet<>(componentDependencies);

    Type[] parameterTypes = method.getGenericParameterTypes();
    Annotation[][] parameterAnnotations = method.getParameterAnnotations();
    for (int i = 0; i < parameterTypes.length; i++) {
      Type parameterType = parameterTypes[i];
      Object argument = args[i];
      boolean isBindsInstance =
          findAnnotation(parameterAnnotations[i], BindsInstance.class) != null;
      if (isBindsInstance) {
        Annotation qualifier = findQualifier(parameterAnnotations[i]);
        scope.addInstance(Key.of(qualifier, parameterType), argument);
      } else //noinspection SuspiciousMethodCalls Can only succeed when parameterType is a Class.
          if (missingModules.remove(parameterType)) {
        scope.addModule((Class<?>) parameterType, argument);
      } else //noinspection SuspiciousMethodCalls Can only succeed when parameterType is a Class.
          if (missingDependencies.remove(parameterType)) {
        scope.addDependency((Class<?>) parameterType, argument);
      } else {
        throw new IllegalStateException(); // TODO unknown argument type
      }
    }

    if (!missingDependencies.isEmpty()) {
      throw new IllegalStateException(); // TODO throw
    }
    for (Class<?> missingModule : missingModules) {
      scope.addModule(missingModule, null);
    }

    return ComponentInvocationHandler.create(componentClass, scope.build());
  }
}
