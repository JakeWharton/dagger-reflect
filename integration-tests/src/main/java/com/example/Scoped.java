package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Singleton;

@Singleton
@Component(modules = Scoped.Module1.class)
interface Scoped {
  int value();

  @Module
  abstract class Module1 {
    private static final AtomicInteger count = new AtomicInteger(1);

    @Provides @Singleton static int value() {
      return count.getAndIncrement();
    }
  }
}
