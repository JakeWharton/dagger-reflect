Dagger Reflect
==============

A reflection-based implementation of the [Dagger][dagger] dependency injection library for fast IDE builds.

See [my talk (from about 37:25)][talk] for more details on how this came about.


Er, what? Why would I want this?
--------------------------------

Feels like going back to Dagger 1 by Square? Not quite: we keep the benefits of Dagger 2, including the annotation processing compile-time validation in production, but using reflection speeds up development builds.

The normal `dagger` artifact requires the use of `dagger-compiler` as an annotation processor for compile-time validation of your components and code generation for runtime performance.
This is a desirable feature for your CI and release builds, but it slows down iterative development.
By using `dagger-reflect` for only your IDE builds, you have one less annotation processor sitting between you and your running app. This is especially important for Kotlin-only or Java/Kotlin mixed projects using KAPT. And if `dagger-compiler` is your only annotation
processor for a module, using `dagger-reflect` means that **zero** annotation processors run during development.


Can I use this in production?
-----------------------------

No.

Well technically you _can_, but don't. It's slow, inefficient, and lacks the level of validation that normal Dagger usage provides.


Usage
-----

_Replace (or fulfill via gradle.properties) `${VERSION_DAGGER}` and `${VERSION_DAGGER_REFLECT}` with whatever versions you are using._

If you have a dedicated variant for development you can skip the `if` check and add the corresponding dependencies for the development and non-development variants.

### Easy migration, quick APT
This usage type is compatible with the current dagger usage patterns where users of Dagger call into the generated code via `DaggerFooComponent.create()` or `DaggerFooComponent.builder()`. The `reflect-compiler` generates source compatible code as `dagger-compiler` would, which resolves the components via reflection. The drawback is that there's still an annotation processor present, albeit much faster than Dagger.

```gradle
dependencies {
  implementation "com.google.dagger:dagger:${VERSION_DAGGER}"
  if (project.hasProperty('android.injected.invoked.from.ide')) {
    implementation "com.jakewharton.dagger:dagger-reflect:${VERSION_DAGGER_REFLECT}"
    annotationProcessor "com.jakewharton.dagger:dagger-reflect-compiler:${VERSION_DAGGER_REFLECT}"
  } else {
    annotationProcessor "com.google.dagger:dagger-compiler:${VERSION_DAGGER}"
  }
}
```

_(No need to upgrade Proguard shirking configuration with this approach.)_

### Pure reflection, no APT
This usage type replaces everything with reflection, but requires a few trivial code changes across the app.

```gradle
dependencies {
  implementation "com.google.dagger:dagger:${VERSION_DAGGER}"
  if (project.hasProperty('android.injected.invoked.from.ide')) {
    implementation "com.jakewharton.dagger:dagger-reflect:${VERSION_DAGGER_REFLECT}"
  } else {
    implementation "com.jakewharton.dagger:dagger-codegen:${VERSION_DAGGER_REFLECT}"
    annotationProcessor "com.google.dagger:dagger-compiler:${VERSION_DAGGER}"
  }
}
```

To bridge the compatibility between `dagger-compile` generated classes and `dagger-reflect`, you'll have to use `dagger.Dagger` class (from `dagger-codegen` or `dagger-reflect`) as a replacement:
 * `FooComponent foo = DaggerFooComponent.create()`  
   &rarr; `FooComponent foo = Dagger.create(FooComponent.class)`
 * `FooComponent foo = DaggerFooComponent.builder().….build()`  
   &rarr; `FooComponent foo = Dagger.builder(FooComponent.Builder.class).….build()`

For the release build using `dagger-codegen` and shirking you'll need to add this to the configuration:
```proguard
# [dagger-reflect] Make sure to keep entry points that dagger.Dagger reflectively accesses
# fixes: Unable to find generated component implementation com.example.Daggera for component com.example.a

# Keep annotation to be able to match it below
-keep class dagger.Component
# Keep the names of the Component interfaces so we can prefix them with "Dagger"
-keepnames @dagger.Component interface **
# Keep the names of the generated Component implementations so the prefixed lookup succeeds
-keepnames class ** implements @dagger.Component **
# Keep the builder() method in the generated Component to reflectively call it
-keepclassmembers class ** implements @dagger.Component ** {
	public static ** builder();
}
# Keep the create() method in the generated Component to reflectively call it
-keepclassmembers class ** implements @dagger.Component ** {
	public static <2> create();
}
```

### SNAPSHOT

Snapshots of the development version are available in [Sonatype's `snapshots` repository][snap].
Don't forget to suffix `VERSION_DAGGER_REFLECT` above with `-SNAPSHOT`, the rest of the usage is the same.

```gradle
repositories {
  maven { name = "Sonatype SNAPSHOTs"; url = "https://oss.sonatype.org/content/repositories/snapshots/" }
}
```


Compatibility
-------------

 * ProGuard/DexGuard/R8: since the dependency injection entry point (e.g. `@Component.Builder`) is being reflected with either usage case, you'll lose some shrinkability, but the majority of the generated `@Component` code using `@Module`s will be shrinked the same as before.

 * dagger-android: Details soon...


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
 [talk]: https://jakewharton.com/helping-dagger-help-you/
