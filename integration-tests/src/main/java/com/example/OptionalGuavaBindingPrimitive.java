package com.example;

import com.google.common.base.Optional;
import dagger.BindsOptionalOf;
import dagger.Component;
import dagger.Module;
import dagger.Provides;

@SuppressWarnings("Guava") // Explicitly testing Guava support.
@Component(modules = OptionalGuavaBindingPrimitive.Module1.class)
public interface OptionalGuavaBindingPrimitive {
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
