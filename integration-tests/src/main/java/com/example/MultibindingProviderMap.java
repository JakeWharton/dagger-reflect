package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Provider;

@Component(modules = MultibindingProviderMap.Module1.class)
interface MultibindingProviderMap {
  Provider<Map<String, String>> values();

  @Module
  abstract class Module1 {
    static final AtomicInteger oneCount = new AtomicInteger(0);
    static final AtomicInteger twoCount = new AtomicInteger(0);

    @Provides
    @IntoMap
    @StringKey("1")
    static String one() {
      return "one" + oneCount.getAndIncrement();
    }

    @Provides
    @IntoMap
    @StringKey("2")
    static String two() {
      return "two" + twoCount.getAndIncrement();
    }
  }
}
