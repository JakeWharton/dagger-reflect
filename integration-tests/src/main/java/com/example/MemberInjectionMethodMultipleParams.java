package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import javax.inject.Inject;

@Component(modules = MemberInjectionMethodMultipleParams.Module1.class)
interface MemberInjectionMethodMultipleParams {
  void inject(Target target);

  final class Target {
    String one;
    Long two;
    Long two2;
    Integer three;

    @Inject
    void multiple(String one, Long two, Long two2, Integer three) {
      this.one = one;
      this.two = two;
      this.two2 = two;
      this.three = three;
    }
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
