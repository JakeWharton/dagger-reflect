package com.example;

import dagger.Binds;
import dagger.Component;
import dagger.Module;
import dagger.Provides;

@Component(modules = BindsProvider.Module1.class)
interface BindsProvider {
  Number number();

  @Module
  abstract class Module1 {
    @Provides
    static Integer integer() {
      return 42;
    }

    @Binds
    abstract Number number(Integer num);
  }
}
