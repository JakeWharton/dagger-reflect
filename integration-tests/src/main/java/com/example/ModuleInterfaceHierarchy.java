package com.example;

import dagger.Binds;
import dagger.Component;
import dagger.Module;
import dagger.Provides;

@Component(modules = ModuleInterfaceHierarchy.Module1.class)
interface ModuleInterfaceHierarchy {
  Number number();

  @Module
  interface Module1 extends BaseModule {
    @Provides
    static Integer integer() {
      return 42;
    }
  }

  @Module
  interface BaseModule {
    @Binds
    Number number(Integer num);
  }
}
