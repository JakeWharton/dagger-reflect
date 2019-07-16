package com.example;

import dagger.Component;
import dagger.Subcomponent;
import javax.inject.Inject;

@Component
interface SubcomponentBindingInstance {
  Sub sub();

  @Subcomponent
  interface Sub {
    // TODO https://github.com/google/dagger/issues/1550
    // Sub self();
    Target target();
  }

  final class Target {
    public final SubcomponentBindingInstance component;
    public final SubcomponentBindingInstance.Sub subcomponent;

    @Inject
    Target(SubcomponentBindingInstance component, SubcomponentBindingInstance.Sub subcomponent) {
      this.component = component;
      this.subcomponent = subcomponent;
    }
  }
}
