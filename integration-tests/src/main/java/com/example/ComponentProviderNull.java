package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import org.jetbrains.annotations.Nullable;

@Component(modules = ComponentProviderNull.Module1.class)
interface ComponentProviderNull {

  @Nullable
  String string();

  @Module
  abstract class Module1 {
    @Provides
    static @Nullable String string() {
      return null;
    }
  }
}
