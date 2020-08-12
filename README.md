Dagger Reflect
==============

A reflection-based implementation of the [Dagger][dagger] dependency injection library for fast
IDE builds and tests.

More info soon...


**Current release**: [0.3.0](CHANGELOG.md)

Snapshots of the next development version are available in [Sonatype's `snapshots` repository][snap].


Usage
-----

There are two methods of integrating Dagger Reflect to replace Dagger:

### Partial Reflection

This approach still uses an annotation processor to generate implementations of your component
interfaces which then call into the reflection runtime. The annotation processor is fully
incremental and does no validation to ensure minimal overhead.

  * Pros:
    * Your code does not have to change when switching between Dagger and Dagger Reflect
      (modulo limitations below)

  * Cons:
    * The use of an annotation processor still causes a build-time impact

For an Android build, configure your dependencies:

```groovy
dependencies {
  if (properties.containsKey('android.injected.invoked.from.ide')) {
    debugAnnotationProcessor 'com.jakewharton.dagger:dagger-reflect-compiler:0.3.0'
    debugApi 'com.jakewharton.dagger:dagger-reflect:0.3.0' // or debugImplementation
  } else {
    debugAnnotationProcessor "com.google.dagger:dagger-compiler:$daggerVersion"
  }
  releaseAnnotationProcessor "com.google.dagger:dagger-compiler:$daggerVersion"
  api "com.google.dagger:dagger:$daggerVersion" // or implementation
}
```

This will enable Dagger Reflect only for debug builds in the IDE.


### Full Reflection

This approach avoids all annotation processor usage enabling the quickest builds at the expense of
having to change your production Dagger code. In order to avoid the need to 

  * Pros:
    * No annotation processors!

  * Cons:
    * Rewrite bridges into generated code to call into runtime library.
    * Special care has to be taken for R8/ProGuard (for now).

```groovy
dependencies {
  if (properties.containsKey('android.injected.invoked.from.ide')) {
    debugApi 'com.jakewharton.dagger:dagger-reflect:0.3.0' // or debugImplementation  
  } else {
    debugAnnotationProcessor "com.google.dagger:dagger-compiler:$daggerVersion"
    debugApi 'com.jakewharton.dagger:dagger-codegen:0.3.0' // or debugImplementation
  }
  releaseAnnotationProcessor "com.google.dagger:dagger-compiler:$daggerVersion"
  releaseApi 'com.jakewharton.dagger:dagger-codegen:0.3.0' // or releaseImplementation
  api "com.google.dagger:dagger:$daggerVersion" // or implementation
}
```

This will enable Dagger Reflect only for debug builds in the IDE.

When creating a component, builder, or factory in your code, replace calls into generated code with
calls into the static `Dagger` factory with the associated class literal.

```diff
-MyComponent component = DaggerMyComponent.create();
+MyComponent component = Dagger.create(MyComponent.class);
```
```diff
-MyComponent.Factory factory = DaggerMyComponent.factory();
+MyComponent.Factory factory = Dagger.factory(MyComponent.Factory.class);
```
```diff
-MyComponent.Builder builder = DaggerMyComponent.builder();
+MyComponent.Builder builder = Dagger.builder(MyComponent.Builder.class);
```

Using specific Lint rules
-------------------------

There are Lint rules for Dagger reflect to simplify the usage:

  * `WrongRetention`:  
    When using a Dagger related custom annotation (e.g. `MapKey`, `Qualifier`), they require
    a runtime retention by adding `@Retention(RUNTIME)`.

They can be enabled in an Android-project by adding

```groovy
dependencies {
  lintChecks 'com.jakewharton.dagger:dagger-reflect-lint:0.3.0'
}
```

To use them in a non-Android project (blocked by [this issue](https://issuetracker.google.com/issues/112526243)),
you'd need to add the (Android) Lint Gradle Plugin:
```groovy
buildscript {
  repositories {
    google()
  }
  dependencies {
    classpath "com.android.tools.build:gradle:3.1.0" // or higher
  }
}
apply plugin: "com.android.lint"
```

Unsupported Features and Limitations
------------------------------------

### Abstract Classes

Because Dagger Reflect is implemented using a [`Proxy`][proxy], only interface components,
factories, and builders are supported.

### Component Visibility

In order for a factory or builder which is backed by a `Proxy` to create an instance of the
enclosing component which is also backed by a `Proxy`, the component has to be public.

### Producers

Pretty sure no one but Google uses this. PRs welcome.



License
-------

    Copyright 2018 Jake Wharton

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.



 [dagger]: https://github.com/google/dagger/
 [snap]: https://oss.sonatype.org/content/repositories/snapshots/
 [proxy]: https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/reflect/Proxy.html
