package com.example;

import dagger.Component;
import dagger.MembersInjector;
import dagger.Module;
import dagger.Provides;
import javax.inject.Inject;

@Component(modules = MemberInjectorComponent.Module1.class)
interface MemberInjectorComponent {
  MembersInjector<Target> targetInjector();

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
