package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import javax.inject.Inject;
import javax.inject.Qualifier;

@Component(modules = MemberInjectionQualified.Module1.class)
interface MemberInjectionQualified {
  void inject(Target target);

  final class Target {
    @Inject @Foo String fromField;
    String fromMethod;

    @Inject
    void foo(@Foo String foo) {
      fromMethod = foo;
    }
  }

  @Module
  abstract class Module1 {
    @Provides
    static @Foo String foo() {
      return "foo";
    }
  }

  @Qualifier
  @interface Foo {}
}
