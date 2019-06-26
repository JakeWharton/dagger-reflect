package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.LongKey;
import java.util.Map;

@Component(modules = MultibindingMapPrimitiveKey.Module1.class)
interface MultibindingMapPrimitiveKey {
  Map<Long, String> values();

  @Module
  abstract class Module1 {
    @Provides
    @IntoMap
    @LongKey(1L)
    static String one() {
      return "one";
    }

    @Provides
    @IntoMap
    @LongKey(2L)
    static String two() {
      return "two";
    }
  }
}
