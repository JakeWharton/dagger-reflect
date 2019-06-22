package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import javax.inject.Inject;
import javax.inject.Provider;

@Component(modules = JustInTimeGenericNested.Module1.class)
interface JustInTimeGenericNested {
  Registry<String> thing();

  @Module
  abstract class Module1 {
    @Provides
    static String provideString() {
      return "foo";
    }
  }

  final class Registry<T> {

    @Inject
    Registry(JobProvider<T> jobProvider) {}
  }

  final class JobProvider<V> {

    @Inject
    public JobProvider(Provider<Job<V>> jobProvider) {}
  }

  final class Job<R> {

    @Inject
    public Job(Provider<R> provider) {}
  }
}
