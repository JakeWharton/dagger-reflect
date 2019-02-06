package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import javax.inject.Inject;

@Component(modules = MemberInjectionMethodVisibility.Module1.class)
interface MemberInjectionMethodVisibility {
  void inject(Target target);

  final class Target {
    String one;
    Long two;
    Integer three;

    int count;

    @Inject
    protected void one(String one) {
      this.one = one;
      count++;
    }

    @Inject
    void two(Long two) {
      this.two = two;
      count++;
    }

    @Inject
    public void three(Integer three) {
      this.three = three;
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
