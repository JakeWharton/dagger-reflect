package com.example;

import dagger.Binds;
import dagger.Component;
import dagger.Module;
import dagger.Provides;
import org.jetbrains.annotations.Nullable;

@Component(modules = BindsProviderNull.Module1.class)
interface BindsProviderNull {

  @Nullable
  CharSequence string();

  @Module
  abstract class Module1 {
    @Provides
    static @Nullable String string() {
      return null;
    }

    @Binds
    abstract CharSequence charSequence(@Nullable String foo);
  }
}
