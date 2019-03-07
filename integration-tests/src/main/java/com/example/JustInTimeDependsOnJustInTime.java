package com.example;

import dagger.Component;

import javax.inject.Inject;
import javax.inject.Singleton;

@Component
interface JustInTimeDependsOnJustInTime {

  Foo thing();

  final class Foo {
    @Inject Foo(Bar bar) {}
  }

  final class Bar {
    @Inject Bar() {}
  }
}
