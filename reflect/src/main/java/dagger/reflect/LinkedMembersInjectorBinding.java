package dagger.reflect;

import dagger.MembersInjector;

final class LinkedMembersInjectorBinding<T> extends Binding.LinkedBinding<MembersInjector<T>> {
  private final Class<T> clazz;
  private final Scope scope;

  LinkedMembersInjectorBinding(Class<T> clazz, Scope scope) {
    this.scope = scope;
    this.clazz = clazz;
  }

  @Override
  public MembersInjector<T> get() {
    return ReflectiveMembersInjector.create(clazz, scope);
  }
}
