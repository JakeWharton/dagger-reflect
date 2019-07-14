# @org.junit.Test
-keepattributes RuntimeVisibleAnnotations

# Test methods are invoked reflectively.
-keepclasseswithmembers class * {
  @org.junit.Test <methods>;
}

# Test classes are constructed reflectively.
-keepclassmembers class **.*Test {
  <init>(...);
}

# Annotation to preserve conditions required for tests which otherwise aren't automatically kept.
-keep @dagger.Keep class *

# For Mac OS and it's case-insensitive (by default) filesystem.
-dontusemixedcaseclassnames
