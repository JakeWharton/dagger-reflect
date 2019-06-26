package com.example;

import com.google.common.base.Optional;
import dagger.BindsOptionalOf;
import dagger.Component;
import dagger.Module;
import dagger.Provides;

@SuppressWarnings("Guava") // Explicitly testing Guava support.
@Component(modules = OptionalGuavaBinding.Module1.class)
public interface OptionalGuavaBinding {
  Optional<String> string();

  @Module
  abstract class Module1 {
    @Provides
    static String foo() {
      return "foo";
    }

    @BindsOptionalOf
    abstract String optionalFoo();
  }
}
