package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Provider;

@Component(modules = MultibindingProviderMapIndirectionCycle.Module1.class)
public interface MultibindingProviderMapIndirectionCycle {
  Factory factory();

  @Module
  abstract class Module1 {
    @Provides
    @IntoMap
    @StringKey("1")
    static Long one(Factory factory) {
      return 1L;
    }
  }

  class Factory {
    public final Map<String, Provider<Long>> providerMap;

    @Inject
    Factory(Map<String, Provider<Long>> providerMap) {
      this.providerMap = providerMap;
    }
  }
}
