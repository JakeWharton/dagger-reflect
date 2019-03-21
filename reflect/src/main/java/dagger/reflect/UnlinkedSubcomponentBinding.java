package dagger.reflect;

import dagger.reflect.Binding.UnlinkedBinding;

final class UnlinkedSubcomponentBinding extends UnlinkedBinding {
  static UnlinkedBinding forBuilder(Class<?> builderClass) {
    return new UnlinkedSubcomponentBinding(true, builderClass);
  }

  static UnlinkedBinding forFactory(Class<?> factoryClass) {
    return new UnlinkedSubcomponentBinding(false, factoryClass);
  }

  private final boolean isBuilder;
  private final Class<?> cls;

  private UnlinkedSubcomponentBinding(boolean isBuilder, Class<?> cls) {
    this.isBuilder = isBuilder;
    this.cls = cls;
  }

  @Override public LinkedBinding<?> link(Linker linker, Scope scope) {
    Object factory;
    if (isBuilder) {
      factory = ComponentBuilderInvocationHandler.forSubcomponentBuilder(cls, scope);
    } else {
      factory = ComponentFactoryInvocationHandler.forSubcomponentFactory(cls, scope);
    }
    return new LinkedInstanceBinding<>(factory);
  }
}
