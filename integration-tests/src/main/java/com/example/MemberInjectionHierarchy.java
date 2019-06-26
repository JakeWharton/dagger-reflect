package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import javax.inject.Inject;

@Component(modules = MemberInjectionHierarchy.Module1.class)
interface MemberInjectionHierarchy {
  void inject(Subtype target);

  class Base {
    @Inject String baseOne;
    boolean baseCalled;

    @Inject
    void two(String one) {
      baseCalled = true;
    }
  }

  final class Subtype extends Base {
    @Inject String subtypeOne;
    boolean subtypeCalled;

    @Inject
    void subtype(String foo) {
      subtypeCalled = true;
    }
  }

  @Module
  abstract class Module1 {
    @Provides
    static String foo() {
      return "foo";
    }
  }
}
