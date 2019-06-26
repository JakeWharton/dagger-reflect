package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;

@Component(modules = MemberInjectionOrder.Module1.class)
interface MemberInjectionOrder {
  void inject(SubType target);

  abstract class Base {
    List<String> calls = new ArrayList<>();

    @Inject String baseField; // 1

    @Inject
    void baseMethod(String baseParam) { // 2
      calls.add(String.format("baseMethod(%s): %s", baseParam, this));
    }

    @Override
    public String toString() {
      return String.format("baseField=%s", baseField);
    }
  }

  class SubType extends Base {
    @Inject String subField; // 3

    SubType() { // 0
      calls.add("instantiation: " + this);
    }

    @Inject
    void subMethod(String subParam) { // 4
      calls.add(String.format("subMethod(%s): %s", subParam, this));
    }

    @Override
    public String toString() {
      return String.format("%s, subField=%s", super.toString(), subField);
    }
  }

  @Module
  abstract class Module1 {
    @Provides
    static String foo() {
      return "foo";
    }
  }
}
