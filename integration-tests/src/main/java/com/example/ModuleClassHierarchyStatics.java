package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;

@Component(modules = ModuleClassHierarchyStatics.Module1.class)
interface ModuleClassHierarchyStatics {
  String string();

  @Module
  abstract class Module1 extends BaseModule {}

  @Module
  abstract class BaseModule {
    @Provides
    static String string() {
      return "foo";
    }
  }
}
