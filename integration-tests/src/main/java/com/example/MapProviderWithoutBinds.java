package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Named;
import javax.inject.Provider;

@Component(modules = MapProviderWithoutBinds.Module1.class)
interface MapProviderWithoutBinds {
  Map<String, Provider<String>> strings();

  @Module
  abstract class Module1 {
    static final AtomicReference<String> oneValue = new AtomicReference<>("unset one");
    static final AtomicReference<String> twoValue = new AtomicReference<>("unset two");

    @Named("one")
    @Provides
    static String provideBarString() {
      return oneValue.get();
    }

    @Named("two")
    @Provides
    static String provideFooString() {
      return twoValue.get();
    }

    @Provides
    static Map<String, Provider<String>> string(
        @Named("one") Provider<String> one, @Named("two") Provider<String> two) {
      Map<String, Provider<String>> map = new HashMap<>();
      map.put("1", one);
      map.put("2", two);
      return map;
    }
  }
}
