package com.example;

import dagger.Component;

@Component
interface MemberInjectionEmpty {
  void inject(Target target);

  final class Target {}
}
