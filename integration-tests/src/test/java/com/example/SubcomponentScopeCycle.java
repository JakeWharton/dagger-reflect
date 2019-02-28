package com.example;

import dagger.Component;
import dagger.Subcomponent;
import java.lang.annotation.Retention;
import javax.inject.Scope;
import javax.inject.Singleton;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Singleton
@Component
public interface SubcomponentScopeCycle {
  RequestComponent request();

  @Request
  @Subcomponent
  interface RequestComponent {
    SingletonComponent singleton();
  }

  @Singleton
  @Subcomponent
  interface SingletonComponent {
  }

  @Scope
  @Retention(RUNTIME)
  @interface Request {}
}
