package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;

import javax.inject.Inject;
import javax.inject.Provider;

@Component
interface ProviderMultipleGenericIntoJustInTime1 {
  void inject(ThingImpl instance);

//  @Module
//  abstract class Module1 {
//    @Provides
//    static String provideString() {
//      return "foo";
//    }
//  }

  class ThingImpl extends Thing1<Dep> {

    @Inject
    Provider<Dep2> dep2;

  }

  abstract class Thing1<V> extends Thing<V, Dep2> {

  }

  abstract class Thing<V, T> {
    @Inject
    Provider<V> valueProvider;

    @Inject
    Provider<T> tProvider;

    @Inject
    V value;

    @Inject
    T t;

  }


  class Dep {

    @Inject
    Dep() {

    }

  }

  class Dep2 {

    @Inject
    Dep2() {

    }

  }

}
