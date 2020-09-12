package dagger.reflect;

import dagger.MembersInjector;

final class UnlinkedMembersInjectorBinding extends Binding.UnlinkedBinding {
  private final Class<?> targetClass;

  UnlinkedMembersInjectorBinding(Class<?> targetClass) {
    this.targetClass = targetClass;
  }

  @Override
  public LinkedBinding<?> link(Linker linker, Scope scope) {
    MembersInjector<?> membersInjector = ReflectiveMembersInjector.create(targetClass, scope);
    return new LinkedInstanceBinding<>(membersInjector);
  }
}
