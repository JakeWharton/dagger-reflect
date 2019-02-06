package com.example;

import dagger.internal.DaggerCodegen;
import dagger.reflect.DaggerReflect;

enum Backend {
  REFLECT {
    @Override
    <C> C create(Class<C> componentClass) {
      return DaggerReflect.create(componentClass);
    }

    @Override
    <B> B builder(Class<B> builderClass) {
      return DaggerReflect.builder(builderClass);
    }

    @Override
    <F> F factory(Class<F> factoryClass) {
      return DaggerReflect.factory(factoryClass);
    }
  },
  @SuppressWarnings("RefersToDaggerCodegen") // Only referring to our type, not theirs.
  CODEGEN {
    @Override
    <C> C create(Class<C> componentClass) {
      return DaggerCodegen.create(componentClass);
    }

    @Override
    <B> B builder(Class<B> builderClass) {
      return DaggerCodegen.builder(builderClass);
    }

    @Override
    <F> F factory(Class<F> factoryClass) {
      return DaggerCodegen.factory(factoryClass);
    }
  };

  abstract <C> C create(Class<C> componentClass);

  abstract <B> B builder(Class<B> builderClass);

  abstract <F> F factory(Class<F> factoryClass);
}
