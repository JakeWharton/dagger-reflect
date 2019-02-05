package com.example;

import dagger.Binds;
import dagger.Component;
import dagger.Module;
import dagger.Provides;

@Component(modules = ModuleInterfaceHierarchy.Module1.class)
interface ModuleInterfaceHierarchy {
  CharSequence string();

  @Module
  interface Module1 extends BaseModule {
    @Provides static String string() {
      return "foo";
    }
  }

  @Module
  interface BaseModule {
    @Binds CharSequence charSequence(String foo);
  }
}
