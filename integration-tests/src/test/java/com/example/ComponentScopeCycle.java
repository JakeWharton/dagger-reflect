package com.example;

import dagger.Component;
import java.lang.annotation.Retention;
import javax.inject.Scope;
import javax.inject.Singleton;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Singleton
@Component(dependencies = ComponentScopeCycle.RequestComponent.class)
public interface ComponentScopeCycle {
  @Request
  @Component(dependencies = SingletonComponent.class)
  interface RequestComponent {
  }

  @Singleton
  @Component
  interface SingletonComponent {
  }

  @Scope
  @Retention(RUNTIME)
  @interface Request {}
}
