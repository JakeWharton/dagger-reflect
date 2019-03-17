package dagger.reflect;

import dagger.Component;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

final class ReflectiveComponentFactoryParser {
  static <F> F parse(Class<F> cls) {
    if (cls.getAnnotation(Component.Factory.class) == null) {
      throw new IllegalArgumentException(
          cls.getCanonicalName() + " lacks @Component.Factory annotation");
    }

    Class<?> componentClass = cls.getEnclosingClass();
    if (componentClass == null) {
      throw new IllegalArgumentException(cls.getCanonicalName()
          + " is not a nested type inside of a component interface.");
    }

    Component component = componentClass.getAnnotation(Component.class);
    if (component == null) {
      throw new IllegalArgumentException(
          componentClass.getCanonicalName() + " lacks @Component annotation");
    }

    return createFactory(cls, componentClass, component.modules(), component.dependencies(), null);
  }

  private static <F> F createFactory(Class<F> factoryClass, Class<?> componentClass, Class<?>[] modules,
      Class<?>[] dependencies, @Nullable Scope parent) {
    Set<Class<?>> moduleSet = new LinkedHashSet<>();
    Collections.addAll(moduleSet, modules);

    Set<Class<?>> dependencySet = new LinkedHashSet<>();
    Collections.addAll(dependencySet, dependencies);

    return ComponentFactoryInvocationHandler.create(componentClass, factoryClass, moduleSet,
        dependencySet, parent);
  }

  private ReflectiveComponentFactoryParser() {
  }
}
