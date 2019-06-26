package com.example;

import dagger.Component;
import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Singleton;

@Singleton
@Component(modules = LazyScopedBinding.Module1.class)
interface LazyScopedBinding {
  Lazy<String> value();

  @Module
  abstract class Module1 {
    static final AtomicInteger oneCount = new AtomicInteger(0);

    @Singleton
    @Provides
    static String one() {
      return "one" + oneCount.getAndIncrement();
    }
  }
}
