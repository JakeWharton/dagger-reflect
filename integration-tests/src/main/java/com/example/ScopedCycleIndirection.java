package com.example;

import dagger.Binds;
import dagger.Component;
import dagger.Lazy;
import dagger.Module;
import dagger.Provides;
import dagger.multibindings.IntoMap;
import dagger.multibindings.StringKey;
import java.util.Map;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

@Singleton
@Component(modules = ScopedCycleIndirection.Module1.class)
public interface ScopedCycleIndirection {
  String s();

  C c();

  D d();

  @Singleton
  class B {
    public final C c;

    @Inject
    B(C c) {
      this.c = c;
    }
  }

  @Singleton
  class C {
    public final Provider<String> provider;
    @Inject public Lazy<String> lazy;
    @Inject public Provider<Lazy<String>> lazyProvider;

    @Inject
    C(Provider<String> provider) {
      this.provider = provider;
    }
  }

  @Module
  abstract class Module1 {
    @Singleton
    @Provides
    static String s(B b, D d) {
      return "a";
    }

    @Singleton
    @Binds
    @IntoMap
    @StringKey("a")
    abstract Object bindsIntoMap(String s);

    @Singleton
    @Binds
    abstract Object binds(String s);
  }

  @Singleton
  class D {
    public final Map<String, Provider<Object>> providerMap;
    public final Provider<Object> provider;

    @Inject
    D(Map<String, Provider<Object>> providerMap, Provider<Object> provider) {
      this.providerMap = providerMap;
      this.provider = provider;
    }
  }
}
