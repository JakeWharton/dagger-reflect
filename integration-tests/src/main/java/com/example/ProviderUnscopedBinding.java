package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;

import javax.inject.Provider;
import java.util.concurrent.atomic.AtomicInteger;

@Component(modules = ProviderUnscopedBinding.Module1.class)
interface ProviderUnscopedBinding {
  Provider<String> value();

  @Module
  abstract class Module1 {
    static final AtomicInteger oneCount = new AtomicInteger(0);

    @Provides static String one() {
      return "one" + oneCount.getAndIncrement();
    }
  }
}
