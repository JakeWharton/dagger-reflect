package com.example;

import dagger.Component;

@Component
public interface AbstractBuilderClass {
  @Component.Builder
  abstract class Builder {}
}
