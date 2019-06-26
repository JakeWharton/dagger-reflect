package com.example;

import dagger.Component;
import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import java.util.concurrent.atomic.AtomicInteger;

@Component(modules = LazyUnscopedBinding.Module1.class)
interface LazyUnscopedBinding {
  Lazy<String> value();

  @Module
  abstract class Module1 {
    static final AtomicInteger oneCount = new AtomicInteger(0);

    @Provides
    static String one() {
      return "one" + oneCount.getAndIncrement();
    }
  }
}
