package dagger.reflect;

import dagger.Component;
import dagger.Subcomponent;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

final class ReflectiveComponentBuilderParser {
  private static final Class<?>[] NO_DEPENDENCIES = new Class<?>[0];

  static <B> B parse(Class<B> cls) {
    if (cls.getAnnotation(Component.Builder.class) == null) {
      throw new IllegalArgumentException(
          cls.getCanonicalName() + " lacks @Component.Builder annotation");
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

    return createBuilder(cls, componentClass, component.modules(), component.dependencies(), null);
  }

  static <B> B parse(Class<B> cls, Scope parent) {
    Class<?> componentClass = cls.getEnclosingClass();
    if (componentClass == null) {
      throw new IllegalArgumentException(cls.getCanonicalName()
          + " is not a nested type inside of a subcomponent interface.");
    }

    Subcomponent subcomponent = componentClass.getAnnotation(Subcomponent.class);
    if (subcomponent == null) {
      throw new IllegalArgumentException(
          componentClass.getCanonicalName() + " lacks @Component annotation");
    }

    return createBuilder(cls, componentClass, subcomponent.modules(), NO_DEPENDENCIES, parent);
  }

  private static <B> B createBuilder(Class<B> builderClass, Class<?> componentClass, Class<?>[] modules,
      Class<?>[] dependencies, @Nullable Scope parent) {
    Set<Class<?>> moduleSet = new LinkedHashSet<>();
    Collections.addAll(moduleSet, modules);

    Set<Class<?>> dependencySet = new LinkedHashSet<>();
    Collections.addAll(dependencySet, dependencies);

    return ComponentBuilderInvocationHandler.create(componentClass, builderClass, moduleSet,
        dependencySet, parent);
  }
}
