package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import java.util.Map;

@Component(modules = MultibindingMap.Module1.class)
interface MultibindingMap {
  Map<String, String> values();

  @Module
  abstract class Module1 {
    @Provides
    @IntoMap
    @StringKey("1")
    static String one() {
      return "one";
    }

    @Provides
    @IntoMap
    @StringKey("2")
    static String two() {
      return "two";
    }
  }
}
