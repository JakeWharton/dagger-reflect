package com.example;

import dagger.Binds;
import dagger.Component;
import dagger.Module;
import dagger.Provides;

@Component(modules = ModuleClassAndInterfaceHierarchy.Module1.class)
interface ModuleClassAndInterfaceHierarchy {
  Number number();

  @Module
  abstract class Module1 implements BaseModule {
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
