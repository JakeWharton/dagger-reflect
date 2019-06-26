package com.example;

import dagger.Component;
import javax.inject.Inject;

@Component
interface JustInTimeDependsOnJustInTime {

  Foo thing();

  final class Foo {
    @Inject
    Foo(Bar bar) {}
  }

  final class Bar {
    @Inject
    Bar() {}
  }
}
