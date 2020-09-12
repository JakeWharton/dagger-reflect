package com.example;

import dagger.Component;
import dagger.MembersInjector;
import dagger.Module;
import dagger.Provides;
import javax.inject.Inject;

@Component(modules = MemberInjectorWildcardType.Module1.class)
interface MemberInjectorWildcardType {
  MembersInjector<Target<? extends String>> targetInjector();

  final class Target<T> {
    @Inject T foo;
  }

  @Module
  abstract class Module1 {
    @Provides
    static String foo() {
      return "foo";
    }
  }
}
