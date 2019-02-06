package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import javax.inject.Inject;

@Component(modules = MemberInjectionFieldBeforeMethod.Module1.class)
interface MemberInjectionFieldBeforeMethod {
  void inject(Target target);

  final class Target {
    @Inject String foo;
    Boolean fieldBeforeMethod;

    @Inject
    void method(String foo) {
      fieldBeforeMethod = this.foo != null;
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
