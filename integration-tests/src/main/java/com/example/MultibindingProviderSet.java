package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Provider;

@Component(modules = MultibindingProviderSet.Module1.class)
interface MultibindingProviderSet {
  Provider<Set<String>> values();

  @Module
  abstract class Module1 {
    static final AtomicInteger oneCount = new AtomicInteger(0);
    static final AtomicInteger twoCount = new AtomicInteger(0);

    @Provides
    @IntoSet
    static String one() {
      return "one" + oneCount.getAndIncrement();
    }

    @Provides
    @IntoSet
    static String two() {
      return "two" + twoCount.getAndIncrement();
    }
  }
}
