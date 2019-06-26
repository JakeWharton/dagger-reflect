package com.example;

import dagger.BindsOptionalOf;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import java.util.Optional;
import javax.annotation.Nullable;

@Component(modules = OptionalBindingNullable.Module1.class)
public interface OptionalBindingNullable {
  Optional<String> string();

  @Module
  abstract class Module1 {
    @Provides
    @Nullable
    static String foo() {
      return null;
    }

    @BindsOptionalOf
    abstract String optionalFoo();
  }
}
