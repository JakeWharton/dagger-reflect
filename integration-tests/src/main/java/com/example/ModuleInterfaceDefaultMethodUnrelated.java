package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;

@Component(modules = ModuleInterfaceDefaultMethodUnrelated.Module1.class)
interface ModuleInterfaceDefaultMethodUnrelated {

  String string();

  @Module
  interface Module1 extends DefaultMethod {
    @Provides
    static String string() {
      return "foo";
    }

    default void unrelatedMethod() {}
  }

  interface DefaultMethod {
    default void unrelatedMethodInherited() {}
  }
}
