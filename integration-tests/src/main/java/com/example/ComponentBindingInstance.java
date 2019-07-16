package com.example;

import dagger.Component;
import javax.inject.Inject;

@Component
interface ComponentBindingInstance {
  ComponentBindingInstance self();

  Target target();

  final class Target {
    public final ComponentBindingInstance component;

    @Inject
    Target(ComponentBindingInstance component) {
      this.component = component;
    }
  }
}
