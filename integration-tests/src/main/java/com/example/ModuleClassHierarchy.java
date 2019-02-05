package com.example;

import dagger.Binds;
import dagger.Component;
import dagger.Module;
import dagger.Provides;

@Component(modules = ModuleClassHierarchy.Module1.class)
interface ModuleClassHierarchy {
  CharSequence string();

  @Module
  abstract class Module1 extends BaseModule {
    @Provides static String string() {
      return "foo";
    }
  }

  @Module
  abstract class BaseModule {
    @Binds abstract CharSequence charSequence(String foo);
  }
}
