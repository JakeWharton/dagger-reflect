package com.example;

import dagger.Binds;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import java.util.Map;

@Component(modules = BindsIntoMap.Module1.class)
interface BindsIntoMap {
  Map<String, String> strings();

  @Module
  abstract class Module1 {
    @Provides
    static String string() {
      return "foo";
    }

    @Binds
    @IntoMap
    @StringKey("bar")
    abstract String mapString(String foo);
  }
}
