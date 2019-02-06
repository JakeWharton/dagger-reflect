package com.example;

import dagger.Binds;
import dagger.Component;
import dagger.Module;
import dagger.Provides;

@Component(modules = ModuleClassHierarchy.Module1.class)
interface ModuleClassHierarchy {
  Number number();

  @Module
  abstract class Module1 extends BaseModule {
    @Provides
    static Integer integer() {
      return 42;
    }
  }

  @Module
  abstract class BaseModule {
    @Binds
    abstract Number number(Integer num);
  }
}
