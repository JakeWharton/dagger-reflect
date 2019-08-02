package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;

@Component(modules = ModuleAbstractInstanceProvidesMethod.Module1.class)
interface ModuleAbstractInstanceProvidesMethod {

  String string();

  @Module
  abstract class Module1 {
    @Provides
    String string() {
      return "foo";
    }
  }
}
