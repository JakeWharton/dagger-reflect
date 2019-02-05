package com.example;

import dagger.Binds;
import dagger.Component;
import dagger.Module;
import dagger.Provides;

@Component(modules = ModuleClassAndInterfaceHierarchy.Module1.class)
interface ModuleClassAndInterfaceHierarchy {
  CharSequence string();

  @Module
  abstract class Module1 implements BaseModule {
    @Provides static String string() {
      return "foo";
    }
  }

  @Module
  interface BaseModule {
    @Binds CharSequence charSequence(String foo);
  }
}
