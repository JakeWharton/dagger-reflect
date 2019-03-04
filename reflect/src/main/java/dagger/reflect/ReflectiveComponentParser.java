package dagger.reflect;

import dagger.Component;
import dagger.Subcomponent;
import java.lang.annotation.Annotation;
import org.jetbrains.annotations.Nullable;

import static dagger.reflect.Reflection.findScope;

final class ReflectiveComponentParser {
  private static final Class<?>[] NO_DEPENDENCIES = new Class<?>[0];

  static <C> C parse(Class<C> cls) {
    Component component = cls.getAnnotation(Component.class);
    if (component == null) {
      throw new IllegalArgumentException(cls.getCanonicalName() + " lacks @Component annotation");
    }
    return createComponent(cls, component.dependencies(), component.modules(), null);
  }

  static <C> C parse(Class<C> cls, Scope parent) {
    Subcomponent subcomponent = cls.getAnnotation(Subcomponent.class);
    if (subcomponent == null) {
      throw new IllegalArgumentException(
          cls.getCanonicalName() + " lacks @Subcomponent annotation");
    }
    return createComponent(cls, NO_DEPENDENCIES, subcomponent.modules(), parent);
  }

  private static <C> C createComponent(Class<C> cls, Class<?>[] dependencies, Class<?>[] modules,
      @Nullable Scope parent) {
    if (dependencies.length > 0) {
      StringBuilder builder = new StringBuilder(cls.getCanonicalName())
          .append(" declares dependencies [");
      for (int i = 0; i < dependencies.length; i++) {
        if (i > 0) builder.append(", ");
        builder.append(dependencies[i].getCanonicalName());
      }
      builder.append("] and therefore must be created with a builder");
      throw new IllegalArgumentException(builder.toString());
    }

    Annotation scopeAnnotation = findScope(cls.getDeclaredAnnotations());

    Scope.Builder scopeBuilder = new Scope.Builder(parent, scopeAnnotation)
        .justInTimeLookupFactory(new ReflectiveJustInTimeLookupFactory());

    for (Class<?> module : modules) {
      scopeBuilder.addModule(module, null);
    }

    return ComponentInvocationHandler.create(cls, scopeBuilder.build());
  }
}
