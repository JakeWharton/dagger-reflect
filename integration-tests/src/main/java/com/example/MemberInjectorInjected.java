package com.example;

import dagger.Component;
import dagger.MembersInjector;
import dagger.Module;
import dagger.Provides;
import javax.inject.Inject;

@Component(modules = MemberInjectorInjected.Module1.class)
interface MemberInjectorInjected {
  Holder holder();

  final class Holder {
    final MembersInjector<Target> targetInjector;

    @Inject
    Holder(MembersInjector<Target> targetInjector) {
      this.targetInjector = targetInjector;
    }
  }

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
