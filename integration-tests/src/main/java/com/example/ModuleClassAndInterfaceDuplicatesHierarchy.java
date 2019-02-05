package com.example;

import dagger.Binds;
import dagger.Component;
import dagger.Module;
import dagger.Provides;

@Component(modules = ModuleClassAndInterfaceDuplicatesHierarchy.Module1.class)
interface ModuleClassAndInterfaceDuplicatesHierarchy {
  CharSequence string();

  @Module
  abstract class Module1 extends BaseModule implements InterfaceModule {
  }

  @Module
  abstract class BaseModule implements InterfaceModule {
    @Provides static String string() {
      return "foo";
    }
  }

  @Module
  interface InterfaceModule {
    @Binds CharSequence charSequence(String foo);
  }
}
