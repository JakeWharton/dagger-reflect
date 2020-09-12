package com.example;

import dagger.Component;

@Component
public interface MembersInjectionWrongReturnType {
  String inject(Target instance);

  class Target {}
}
