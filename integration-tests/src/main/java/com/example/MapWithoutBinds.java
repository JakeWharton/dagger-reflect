package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import java.util.HashMap;
import java.util.Map;

@Component(modules = MapWithoutBinds.Module1.class)
interface MapWithoutBinds {
  Map<String, String> strings();

  @Module
  abstract class Module1 {
    @Provides
    static Map<String, String> string() {
      Map<String, String> map = new HashMap<>();
      map.put("1", "one");
      map.put("2", "two");
      return map;
    }
  }
}
