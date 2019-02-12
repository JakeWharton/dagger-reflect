package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;

@Component(modules = ProviderCycle.Module1.class)
public interface ProviderCycle {
  String string();

  @Module
  abstract class Module1 {
    @Provides static String longToString(Long value) {
      return String.valueOf(value);
    }

    @Provides static Integer stringToInteger(String value) {
      return Integer.parseInt(value);
    }

    @Provides static Long intToLong(Integer value) {
      return Long.valueOf(value);
    }
  }
}
