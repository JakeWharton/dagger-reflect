package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;

@Component(modules = ImplicitModuleInstance.Module1.class)
public interface ImplicitModuleInstance {

  String string();

  @Module
  class Module1 {
    @Provides
    String string() {
      return "one";
    }
  }
}
