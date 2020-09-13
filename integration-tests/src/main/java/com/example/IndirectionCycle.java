package com.example;

import dagger.Component;
import dagger.Lazy;
import javax.inject.Inject;
import javax.inject.Provider;

@Component
public interface IndirectionCycle {
  A a();

  C c();

  class A {
    public final B b;

    @Inject
    A(B b) {
      this.b = b;
    }
  }

  class B {
    public final C c;

    @Inject
    B(C c) {
      this.c = c;
    }
  }

  class C {
    public final Provider<A> providerA;
    @Inject public Lazy<A> lazyA;
    @Inject public Provider<Lazy<A>> lazyProviderA;

    @Inject
    C(Provider<A> providerA) {
      this.providerA = providerA;
    }
  }
}
