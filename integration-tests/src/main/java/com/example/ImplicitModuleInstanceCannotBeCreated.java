package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;

@Component(modules = ImplicitModuleInstanceCannotBeCreated.Module1.class)
public interface ImplicitModuleInstanceCannotBeCreated {

  String string();

  @Module
  class Module1 {

    Module1() {
      throw new IllegalStateException("No need to instantiate this");
    }

    @Provides
    static String string() {
      return "one";
    }

    void justAnInstanceMethod() {}
  }
}
