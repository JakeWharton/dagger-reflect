package com.example;

import dagger.Component;

@Component
interface MemberInjectionEmptyInterface {

  void inject(Target target);

  interface Target {}
}
