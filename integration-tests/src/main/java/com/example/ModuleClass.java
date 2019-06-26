package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;

@Component(modules = ModuleClass.Module1.class)
interface ModuleClass {
  String string();

  @Module
  abstract class Module1 {
    @Provides
    static String string() {
      return "foo";
    }
  }
}
