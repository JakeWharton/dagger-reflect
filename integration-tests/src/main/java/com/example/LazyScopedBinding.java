package com.example;

import dagger.Component;
import dagger.Lazy;
import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;
import java.util.concurrent.atomic.AtomicInteger;

@Singleton
@Component(modules = LazyScopedBinding.Module1.class)
interface LazyScopedBinding {
  Lazy<String> value();

  @Module
  abstract class Module1 {
    static final AtomicInteger oneCount = new AtomicInteger(0);

    @Singleton @Provides static String one() {
      return "one" + oneCount.getAndIncrement();
    }
  }
}
