package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;

@Component(modules = MemberInjectionInterface.Module1.class)
interface MemberInjectionInterface {

  void inject(Target target);

  interface Target {
    String FOO = "foo";

    void method(String foo);
  }

  @Module
  abstract class Module1 {
    @Provides
    static String foo() {
      return "foo";
    }
  }
}
