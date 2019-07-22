package com.example;

import dagger.Component;

@Component(dependencies = NestedDependencyInterfaceTest.First.class)
public interface NestedDependencyInterfaceTest {
  String value();

  @Component.Factory
  interface Factory {
    NestedDependencyInterfaceTest create(First dependencies);
  }

  interface First extends Second {}

  interface Second extends Third {}

  interface Third {
    String value();
  }
}
