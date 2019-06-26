package com.example;

import dagger.Binds;
import dagger.Component;
import dagger.Module;
import dagger.Provides;

@Component(modules = ModuleClassAndInterfaceDuplicatesHierarchy.Module1.class)
interface ModuleClassAndInterfaceDuplicatesHierarchy {
  Number number();

  @Module
  abstract class Module1 extends BaseModule implements InterfaceModule {}

  @Module
  abstract class BaseModule implements InterfaceModule {
    @Provides
    static Integer integer() {
      return 42;
    }
  }

  @Module
  interface InterfaceModule {
    @Binds
    Number number(Integer num);
  }
}
