package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;

@Component(modules = ModuleInterfaceDefaultProvidesMethod.Module1.class)
interface ModuleInterfaceDefaultProvidesMethod {

  String string();

  @Module
  interface Module1 {
    @Provides
    default String string() {
      return "foo";
    }
  }
}
