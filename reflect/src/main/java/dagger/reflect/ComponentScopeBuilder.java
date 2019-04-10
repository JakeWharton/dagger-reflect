package dagger.reflect;

import dagger.Component;
import dagger.Module;
import dagger.Subcomponent;
import java.lang.annotation.Annotation;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.jetbrains.annotations.Nullable;

import static dagger.reflect.Reflection.findEnclosedAnnotatedClass;
import static dagger.reflect.Reflection.findScope;
import static dagger.reflect.Reflection.requireAnnotation;

final class ComponentScopeBuilder {
  static ComponentScopeBuilder buildComponent(Class<?> componentClass) {
    Component component = requireAnnotation(componentClass, Component.class);
    Annotation scopeAnnotation = findScope(componentClass.getDeclaredAnnotations());
    return create(component.modules(), component.dependencies(), scopeAnnotation, null);
  }

  static ComponentScopeBuilder buildSubcomponent(Class<?> subcomponentClass, Scope parent) {
    Subcomponent subcomponent = requireAnnotation(subcomponentClass, Subcomponent.class);
    Annotation scopeAnnotation = findScope(subcomponentClass.getDeclaredAnnotations());
    return create(subcomponent.modules(), new Class<?>[0], scopeAnnotation, parent);
  }

  private static ComponentScopeBuilder create(Class<?>[] moduleClasses, Class<?>[] dependencyClasses,
      @Nullable Annotation scopeAnnotation, @Nullable Scope parent) {
    Map<Class<?>, Object> moduleInstances = new LinkedHashMap<>();
    Set<Class<?>> subcomponentClasses = new LinkedHashSet<>();

    Deque<Class<?>> moduleClassQueue = new ArrayDeque<>();
    Collections.addAll(moduleClassQueue, moduleClasses);
    while (!moduleClassQueue.isEmpty()) {
      Class<?> moduleClass = moduleClassQueue.removeFirst();
      Module module = requireAnnotation(moduleClass, Module.class);

      Collections.addAll(moduleClassQueue, module.includes());
      Collections.addAll(subcomponentClasses, module.subcomponents());

      // Start with all modules bound to null. Any remaining nulls will be assumed stateless.
      moduleInstances.put(moduleClass, null);
    }

    Map<Class<?>, Object> dependencyInstances = new LinkedHashMap<>();
    for (Class<?> dependencyClass : dependencyClasses) {
      // Start with all dependencies as null. Any remaining nulls at creation time is an error.
      dependencyInstances.put(dependencyClass, null);
    }

    return new ComponentScopeBuilder(moduleInstances, dependencyInstances, subcomponentClasses,
        scopeAnnotation, parent);
  }

  private final Map<Key, Object> boundInstances = new LinkedHashMap<>();
  private final Map<Class<?>, Object> moduleInstances;
  private final Map<Class<?>, Object> dependencyInstances;
  private final Set<Class<?>> subcomponentClasses;
  private final @Nullable Annotation scopeAnnotation;
  private final @Nullable Scope parent;

  private ComponentScopeBuilder(Map<Class<?>, Object> moduleInstances,
      Map<Class<?>, Object> dependencyInstances, Set<Class<?>> subcomponentClasses,
      @Nullable Annotation scopeAnnotation, @Nullable Scope parent) {
    this.moduleInstances = moduleInstances;
    this.dependencyInstances = dependencyInstances;
    this.subcomponentClasses = subcomponentClasses;
    this.scopeAnnotation = scopeAnnotation;
    this.parent = parent;
  }

  void putBoundInstance(Key key, Object instance) {
    boundInstances.put(key, instance);
  }

  /** @throws IllegalArgumentException when {@code moduleClass} is not in expected set. */
  void setModule(Class<?> moduleClass, Object instance) {
    if (moduleInstances.containsKey(moduleClass)) {
      moduleInstances.put(moduleClass, instance);
    } else {
      throw new IllegalArgumentException("Module "
          + moduleClass.getName()
          + " not in expected transitive set: "
          + moduleInstances.keySet());
    }
  }

  /** @throws IllegalArgumentException when {@code dependencyClass} is not in expected set. */
  void setDependency(Class<?> dependencyClass, Object instance) {
    if (dependencyInstances.containsKey(dependencyClass)) {
      dependencyInstances.put(dependencyClass, instance);
    } else {
      throw new IllegalArgumentException("Dependency "
          + dependencyClass.getName()
          + " not in expected transitive set: "
          + dependencyInstances.keySet());
    }
  }

  Scope build() {
    Scope.Builder scopeBuilder = new Scope.Builder(parent, scopeAnnotation)
        .justInTimeLookupFactory(new ReflectiveJustInTimeLookupFactory());

    for (Map.Entry<Key, Object> entry : boundInstances.entrySet()) {
      scopeBuilder.addInstance(entry.getKey(), entry.getValue());
    }

    for (Map.Entry<Class<?>, Object> entry : moduleInstances.entrySet()) {
      Object instance = entry.getValue();
      if (instance != null) {
        scopeBuilder.addModule(instance);
      } else {
        scopeBuilder.addModule(entry.getKey());
      }
    }

    for (Map.Entry<Class<?>, Object> entry : dependencyInstances.entrySet()) {
      Class<?> type = entry.getKey();
      Object instance = entry.getValue();
      if (instance == null) {
        throw new IllegalStateException(type.getCanonicalName() + " must be set");
      }
      scopeBuilder.addDependency(type, instance);
    }

    for (Class<?> subcomponentClass : subcomponentClasses) {
      Class<?> builderClass =
          findEnclosedAnnotatedClass(subcomponentClass, Subcomponent.Builder.class);
      Class<?> factoryClass =
          findEnclosedAnnotatedClass(subcomponentClass, Subcomponent.Factory.class);

      if (builderClass != null && factoryClass != null) {
        throw new IllegalStateException(
            "@Subcomponent has more than one @Subcomponent.Builder or @Subcomponent.Factory: ["
                + builderClass.getCanonicalName()
                + ", "
                + factoryClass.getCanonicalName()
                + "]");
      } else if (builderClass != null) {
        scopeBuilder.addBinding(Key.of(null, builderClass),
            UnlinkedSubcomponentBinding.forBuilder(builderClass));
      } else if (factoryClass != null) {
        scopeBuilder.addBinding(Key.of(null, factoryClass),
            UnlinkedSubcomponentBinding.forFactory(factoryClass));
      } else {
        throw new IllegalStateException(subcomponentClass.getCanonicalName()
            + " doesn't have a @Subcomponent.Builder or @Subcomponent.Factory,"
            + " which is required when used with @Module.subcomponents");
      }
    }

    return scopeBuilder.build();
  }
}
