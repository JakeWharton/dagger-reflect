package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import javax.annotation.Nullable;

@Component(modules = BindsProviderNullabilityMismatch.Module1.class)
interface BindsProviderNullabilityMismatch {

  String string();

  @Module
  abstract class Module1 {
    @Provides
    static @Nullable String string() {
      return null;
    }
  }
}
