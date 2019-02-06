package com.example;

import dagger.BindsOptionalOf;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import java.util.Optional;

@Component(modules = OptionalBinding.Module1.class)
public interface OptionalBinding {
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
