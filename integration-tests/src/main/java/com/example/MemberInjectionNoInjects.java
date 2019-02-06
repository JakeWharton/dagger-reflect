package com.example;

import dagger.Component;

@Component
interface MemberInjectionNoInjects {
  void inject(Target target);

  final class Target {
    protected String one;
    Long two;
    public Integer three;

    int count;

    protected void one(String one) {
      count++;
    }

    void two(Long two) {
      count++;
    }

    public void three(Integer three) {
      count++;
    }
  }
}
