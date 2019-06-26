package com.example;

import dagger.Binds;
import dagger.Component;
import dagger.Module;
import dagger.Provides;

@Component(modules = ModuleInterface.Module1.class)
interface ModuleInterface {
  Number number();

  @Module
  interface Module1 {
    @Provides
    static Integer integer() {
      return 42;
    }

    @Binds
    Number number(Integer num);
  }
}
