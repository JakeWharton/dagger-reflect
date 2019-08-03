package com.example;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import dagger.BindsOptionalOf;
import dagger.Component;
import dagger.Module;
import java.lang.annotation.Retention;
import java.util.Optional;
import javax.inject.Inject;
import javax.inject.Qualifier;
import javax.inject.Scope;
import javax.inject.Singleton;

@Singleton
@Component(modules = OptionalBindingWrongScope.Module1.class)
public interface OptionalBindingWrongScope {
  @Qualified
  Optional<Thing> thing();

  @Module
  abstract class Module1 {
    @BindsOptionalOf
    @Qualified
    abstract Thing optionalThing();
  }

  @Qualified // @BindsOptionalOf cannot work with unqualified JIT, so a @Qualifier is needed
  @Unrelated // mismatched scope (to Singleton) makes putJitBinding return null
  final class Thing {
    @Inject
    Thing() {}
  }

  @Scope
  @Retention(RUNTIME)
  @interface Unrelated {}

  @Qualifier
  // @Retention(RUNTIME) missing, so that binding is not found
  @interface Qualified {}
}
