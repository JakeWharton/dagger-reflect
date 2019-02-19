package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;

@Component(modules = ImplicitFinalModules.Module1.class)
public interface ImplicitFinalModules {

  String string();

  @Module final class Module1 {

    public static final String VALUE = "one";

    @Provides String string() {
      return VALUE;
    }
  }
}
