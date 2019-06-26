package com.example;

import dagger.BindsOptionalOf;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import java.util.Optional;

@Component(modules = OptionalBindingPrimitive.Module1.class)
public interface OptionalBindingPrimitive {
  Optional<Long> five();

  @Module
  abstract class Module1 {
    @Provides
    static long five() {
      return 5L;
    }

    @BindsOptionalOf
    abstract long optionalFive();
  }
}
