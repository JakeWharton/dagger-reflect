package com.example;

import dagger.Component;

@Component
public interface MembersInjectorWrongReturnType {
  String inject(Target instance);

  class Target {}
}
