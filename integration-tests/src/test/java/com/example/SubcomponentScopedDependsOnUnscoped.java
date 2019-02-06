package com.example;

import dagger.Component;
import dagger.Subcomponent;
import javax.inject.Singleton;

@Component
public interface SubcomponentScopedDependsOnUnscoped {
  ScopedComponent scoped();

  @Singleton
  @Subcomponent
  interface ScopedComponent {}
}
