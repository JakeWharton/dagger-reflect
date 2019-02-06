package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;

@Component(modules = ModulePrivateMethod.ModuleWithPrivateMethods.class)
public interface ModulePrivateMethod {

  Integer integer();

  @Module
  class ModuleWithPrivateMethods {
    @Provides
    Integer integer() {
      return helperPrivateMethod();
    }

    private Integer helperPrivateMethod() {
      return helperPrivateStaticMethod();
    }

    private static Integer helperPrivateStaticMethod() {
      return 42;
    }
  }
}
