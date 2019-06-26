package com.example;

import dagger.Component;
import javax.inject.Singleton;

@Singleton
@Component(dependencies = ComponentScopedDependsOnUnscoped.UnscopedComponent.class)
public interface ComponentScopedDependsOnUnscoped {
  @Component
  interface UnscopedComponent {}
}
