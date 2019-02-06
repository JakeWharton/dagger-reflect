package com.example;

import dagger.Binds;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoSet;
import java.util.Set;

@Component(modules = BindsIntoSet.Module1.class)
interface BindsIntoSet {
  Set<String> strings();

  @Module
  abstract class Module1 {
    @Provides
    static String string() {
      return "foo";
    }

    @Binds
    @IntoSet
    abstract String setString(String foo);
  }
}
