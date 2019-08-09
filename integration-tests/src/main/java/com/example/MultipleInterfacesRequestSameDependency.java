package com.example;

import dagger.Component;

@Component(dependencies = MultipleInterfacesRequestSameDependency.Aggregate.class)
public interface MultipleInterfacesRequestSameDependency {
  String value();

  @Component.Factory
  interface Factory {
    MultipleInterfacesRequestSameDependency create(Aggregate first);
  }

  interface Aggregate extends Host1, Host2 {
    @Override
    String value();
  }

  interface Host1 {
    String value();
  }

  interface Host2 {
    String value();
  }
}
