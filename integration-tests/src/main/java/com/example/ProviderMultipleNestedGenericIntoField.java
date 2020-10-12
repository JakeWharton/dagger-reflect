package com.example;

import dagger.Component;
import javax.inject.Inject;
import javax.inject.Provider;

@Component
interface ProviderMultipleNestedGenericIntoField {
  void inject(ThingImpl instance);

  class ThingImpl extends Thing1<Dep1> {

    @Inject Provider<Dep2> dep2;
  }

  abstract class Thing1<V> extends Thing<V, Dep2> {}

  abstract class Thing<V, T> {
    @Inject Provider<V> vProvider;

    @Inject Provider<T> tProvider;

    @Inject V value;

    @Inject T t;
  }

  class Dep1 {

    @Inject
    Dep1() {}
  }

  class Dep2 {

    @Inject
    Dep2() {}
  }
}
