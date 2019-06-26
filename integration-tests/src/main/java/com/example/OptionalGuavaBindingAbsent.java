package com.example;

import com.google.common.base.Optional;
import dagger.BindsOptionalOf;
import dagger.Component;
import dagger.Module;

@SuppressWarnings("Guava") // Explicitly testing Guava support.
@Component(modules = OptionalGuavaBindingAbsent.Module1.class)
public interface OptionalGuavaBindingAbsent {
  Optional<String> string();

  @Module
  abstract class Module1 {
    @BindsOptionalOf
    abstract String optionalFoo();
  }
}
