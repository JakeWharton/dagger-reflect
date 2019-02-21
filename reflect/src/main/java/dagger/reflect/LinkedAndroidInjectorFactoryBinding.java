package dagger.reflect;

import dagger.android.AndroidInjector;
import dagger.reflect.Binding.LinkedBinding;

import java.lang.annotation.Annotation;
import java.util.Set;

final class LinkedAndroidInjectorFactoryBinding<T>
    extends LinkedBinding<AndroidInjector.Factory<T>> {
  private final Scope scope;
  private final Class<?>[] moduleClasses;
  private final Class<T> instanceClass;
  private final Set<Annotation> annotations;

  LinkedAndroidInjectorFactoryBinding(Scope scope, Class<?>[] moduleClasses,
                                      Class<T> instanceClass, Set<Annotation> annotations) {
    this.scope = scope;
    this.moduleClasses = moduleClasses;
    this.instanceClass = instanceClass;
    this.annotations = annotations;
  }

  @Override public AndroidInjector.Factory<T> get() {
    return new ReflectiveAndroidInjector.Factory<>(scope, moduleClasses, instanceClass, annotations);
  }
}
