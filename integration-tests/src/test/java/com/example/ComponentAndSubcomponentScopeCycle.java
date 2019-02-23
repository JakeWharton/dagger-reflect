package com.example;

import dagger.Component;
import dagger.Subcomponent;
import java.lang.annotation.Retention;
import javax.inject.Scope;
import javax.inject.Singleton;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@ComponentAndSubcomponentScopeCycle.Request
@Component(dependencies = ComponentAndSubcomponentScopeCycle.UpstreamSingletonComponent.class)
public interface ComponentAndSubcomponentScopeCycle {
  SingletonSubcomponent singleton();

  @Request
  @Component(dependencies = SingletonSubcomponent.class)
  interface UpstreamSingletonComponent {
  }

  @Singleton
  @Subcomponent
  interface SingletonSubcomponent {
  }

  @Scope
  @Retention(RUNTIME)
  @interface Request {}
}
