package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Provider;

@Component(modules = MultibindingMapProvider.Module1.class)
interface MultibindingMapProvider {
  Map<String, Provider<String>> values();

  @Module
  abstract class Module1 {
    static final AtomicReference<String> oneValue = new AtomicReference<>("unset one");
    static final AtomicReference<String> twoValue = new AtomicReference<>("unset two");

    @Provides
    @IntoMap
    @StringKey("1")
    static String one() {
      return oneValue.get();
    }

    @Provides
    @IntoMap
    @StringKey("2")
    static String two() {
      return twoValue.get();
    }
  }
}
