package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import javax.inject.Inject;

@Component(modules = MemberInjectionReturnInstance.Module1.class)
interface MemberInjectionReturnInstance {
  Target inject(Target target);

  final class Target {
    @Inject String foo;
  }

  @Module
  abstract class Module1 {
    @Provides
    static String foo() {
      return "foo";
    }
  }
}
