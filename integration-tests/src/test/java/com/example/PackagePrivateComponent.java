package com.example;

import dagger.Component;

@Component
interface PackagePrivateComponent {
  @Component.Builder
  interface Builder {
    PackagePrivateComponent build();
  }
}
