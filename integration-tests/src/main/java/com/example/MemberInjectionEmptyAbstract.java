package com.example;

import dagger.Component;

@Component
interface MemberInjectionEmptyAbstract {

  void inject(Target target);

  abstract class Target {}
}
