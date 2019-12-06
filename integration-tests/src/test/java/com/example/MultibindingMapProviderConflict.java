package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import java.util.Collections;
import java.util.Map;
import javax.inject.Provider;

@Component(modules = MultibindingMapProviderConflict.Module1.class)
interface MultibindingMapProviderConflict {
  Map<String, Provider<Integer>> values();

  @Module
  abstract class Module1 {
    @Provides
    @IntoMap
    @StringKey("first")
    static Integer one() {
      return 1;
    }

    @Provides
    static Map<String, Provider<Integer>> explicit() {
      return Collections.emptyMap();
    }
  }
}
