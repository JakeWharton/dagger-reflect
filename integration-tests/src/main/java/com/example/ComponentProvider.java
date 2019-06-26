package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;

@Component(modules = ComponentProvider.Module1.class)
interface ComponentProvider {
  String string();

  @Module
  abstract class Module1 {
    @Provides
    static String string() {
      return "foo";
    }
  }
}
