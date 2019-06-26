package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import javax.inject.Inject;

@Component(modules = MemberInjectionMethodReturnTypes.Module1.class)
interface MemberInjectionMethodReturnTypes {
  void inject(Target target);

  final class Target {
    int count;

    @Inject
    String one(String one) {
      count++;
      return one;
    }

    @Inject
    long two(Long two) {
      count++;
      return two;
    }

    @Inject
    void three(Integer three) {
      count++;
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
