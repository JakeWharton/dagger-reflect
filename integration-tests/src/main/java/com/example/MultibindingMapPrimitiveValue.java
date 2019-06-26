package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import java.util.Map;

@Component(modules = MultibindingMapPrimitiveValue.Module1.class)
interface MultibindingMapPrimitiveValue {
  Map<String, Long> values();

  @Module
  abstract class Module1 {
    @Provides
    @IntoMap
    @StringKey("1")
    static long one() {
      return 1L;
    }

    @Provides
    @IntoMap
    @StringKey("2")
    static long two() {
      return 2L;
    }
  }
}
