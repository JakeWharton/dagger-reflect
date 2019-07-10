package com.example;

import dagger.Component;
import dagger.Module;
import dagger.Provides;

@Component(modules = ComponentBindingInstance.Module1.class)
interface ComponentBindingInstance {

  Result result();

  final class Result {
    public final ComponentBindingInstance foo;

    public Result(ComponentBindingInstance foo) {
      this.foo = foo;
    }
  }

  @Module
  abstract class Module1 {
    @Provides
    static Result foo(ComponentBindingInstance component) {
      return new Result(component);
    }
  }
}
