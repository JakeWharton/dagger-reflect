package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import java.util.Collections;
import java.util.Map;

@Component(modules = MultibindingMapConflict.Module1.class)
interface MultibindingMapConflict {
  Map<String, Integer> values();

  @Module
  abstract class Module1 {
    @Provides
    @IntoMap
    @StringKey("first")
    static Integer one() {
      return 1;
    }

    @Provides
    static Map<String, Integer> explicit() {
      return Collections.emptyMap();
    }
  }
}
