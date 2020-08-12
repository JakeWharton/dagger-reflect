package com.example;

import dagger.Binds;
import dagger.Component;
import dagger.Lazy;
import dagger.Module;
import javax.inject.Inject;
import javax.inject.Provider;

@Component(modules = BindsIndirectionCycle.Module1.class)
public interface BindsIndirectionCycle {
  B b();

  class A {
    public final B b;

    @Inject
    A(B b) {
      this.b = b;
    }
  }

  class B {
    public final Provider<Object> providerObject;
    @Inject public Lazy<Object> lazyObject;
    @Inject public Provider<Lazy<Object>> lazyProviderObject;

    @Inject
    B(Provider<Object> providerObject) {
      this.providerObject = providerObject;
    }
  }

  @Module
  abstract class Module1 {
    @Binds
    abstract Object bindsA(A a);
  }
}
