package com.example;

import dagger.Module;
import dagger.Provides;

@Module
abstract class StringModule {
  @Provides static String string() {
    return "Hello!";
  }
}
