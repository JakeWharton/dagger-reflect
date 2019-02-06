package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import javax.inject.Inject;

@Component(modules = MemberInjectionFieldVisibility.Module1.class)
interface MemberInjectionFieldVisibility {
  void inject(Target target);

  final class Target {
    @Inject protected String one;
    @Inject Long two;
    @Inject public Integer three;
  }

  @Module
  abstract class Module1 {
    @Provides
    static String one() {
      return "one";
    }

    @Provides
    static Long two() {
      return 2L;
    }

    @Provides
    static Integer three() {
      return 3;
    }
  }
}
