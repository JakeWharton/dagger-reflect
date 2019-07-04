package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.ClassKey;
import dagger.multibindings.IntoMap;
import java.util.Map;

@Component(modules = MultibindingMapClassKey.Module1.class)
interface MultibindingMapClassKey {

  Map<Class<?>, I> values();

  @Module
  abstract class Module1 {

    @Provides
    @IntoMap
    @ClassKey(Impl1.class)
    static I one() {
      return Impl1.INSTANCE;
    }

    @Provides
    @IntoMap
    @ClassKey(Impl2.class)
    static I two() {
      return Impl2.INSTANCE;
    }
  }
}

interface I {}

enum Impl1 implements I {
  INSTANCE
}

enum Impl2 implements I {
  INSTANCE
}
