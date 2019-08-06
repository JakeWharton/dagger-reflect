package dagger.reflect;

import java.lang.annotation.Annotation;
import java.util.Set;

final class UnlinkedAndroidInjectorFactoryBinding extends Binding.UnlinkedBinding {
  private final Class<?>[] moduleClasses;
  private final Class<?> instanceClass;
  private final Set<Annotation> annotations;

  UnlinkedAndroidInjectorFactoryBinding(
      Class<?>[] moduleClasses, Class<?> instanceClass, Set<Annotation> annotations) {
    this.moduleClasses = moduleClasses;
    this.instanceClass = instanceClass;
    this.annotations = annotations;
  }

  @Override
  public LinkedBinding<?> link(Linker linker, Scope scope) {
    ReflectiveAndroidInjector.Factory<?> factory =
        new ReflectiveAndroidInjector.Factory<>(scope, moduleClasses, instanceClass, annotations);
    return new LinkedInstanceBinding<>(factory);
  }
}
