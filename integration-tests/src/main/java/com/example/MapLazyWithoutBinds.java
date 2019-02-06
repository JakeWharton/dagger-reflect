package com.example;

import dagger.Component;
import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.inject.Named;

@Component(modules = MapLazyWithoutBinds.Module1.class)
interface MapLazyWithoutBinds {
  Map<String, Lazy<String>> strings();

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
    static Map<String, Lazy<String>> string(
        @Named("one") Lazy<String> one, @Named("two") Lazy<String> two) {
      Map<String, Lazy<String>> map = new HashMap<>();
      map.put("1", one);
      map.put("2", two);
      return map;
    }
  }
}
