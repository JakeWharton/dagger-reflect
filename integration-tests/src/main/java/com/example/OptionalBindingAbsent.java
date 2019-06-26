package com.example;

import dagger.BindsOptionalOf;
import dagger.Component;
import dagger.Module;
import java.util.Optional;

@Component(modules = OptionalBindingAbsent.Module1.class)
public interface OptionalBindingAbsent {
  Optional<String> string();

  @Module
  abstract class Module1 {
    @BindsOptionalOf
    abstract String optionalFoo();
  }
}
