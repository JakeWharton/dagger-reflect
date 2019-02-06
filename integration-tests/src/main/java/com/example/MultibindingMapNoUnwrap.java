package com.example;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import dagger.Component;
import dagger.MapKey;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import java.lang.annotation.Retention;
import java.util.Map;

@Component(modules = MultibindingMapNoUnwrap.Module1.class)
interface MultibindingMapNoUnwrap {
  Map<TableKey, String> values();

  @Module
  abstract class Module1 {
    @Provides
    @IntoMap
    @TableKey(row = 1, col = 1)
    static String one() {
      return "one";
    }

    @Provides
    @IntoMap
    @TableKey(row = 2, col = 3)
    static String two() {
      return "two";
    }
  }

  @Retention(RUNTIME)
  @MapKey(unwrapValue = false)
  @interface TableKey {
    int row();

    int col();
  }
}
