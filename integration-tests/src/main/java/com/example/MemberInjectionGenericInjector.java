package com.example;

import dagger.BindsInstance;
import dagger.Component;
import javax.inject.Inject;

@Component
public interface MemberInjectionGenericInjector
    extends Injector<MemberInjectionGenericInjector.Target> {

  @Component.Factory
  interface Factory {
    MemberInjectionGenericInjector create(@BindsInstance String one);
  }

  class Target {
    @Inject String one;
  }
}

interface Injector<T> {
  void inject(T target);
}
