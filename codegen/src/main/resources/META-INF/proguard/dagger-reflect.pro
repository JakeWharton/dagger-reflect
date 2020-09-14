# When using factories or builders, the enclosing class is used to determine the component type.
-keepattributes InnerClasses
-keepattributes EnclosingMethod # Required by InnerClasses

# JSR 330 and Dagger annotations like @Inject, @Provide, @Component, etc. are obviously needed.
-keepattributes RuntimeVisibleAnnotations

# Annotation defaults are required for @MapKey, @Component, @Subcomponent, etc.
-keepattributes AnnotationDefault

# Generic signatures are needed for properly parsing types of module methods, component methods,
# and injected fields/methods.
-keepattributes Signature

# The names of component and subcomponent types must be kept so the Dagger-generated type can be
# resolved by prepending a prefix to the name. This should also prevent vertical class merging.
-keepnames @dagger.Component class *
-keepnames @dagger.Subcomponent class *

# For each component type, keep the corresponding Dagger-generated type. We also keep the create
# method, in case there is no builder or factory.
-if @dagger.Component class **.*
-keep class <1>.Dagger<2> {
  static ** create();
}

# For each component builder type that is kept, keep the name of that type which will ensure it
# remains nested inside its corresponding component type.
-if @dagger.Component$Builder class **.*$*
-keep class <1>.<2>

# For each component builder type that is kept, keep the corresponding Dagger-generated component
# type and the factory method for the builder.
-if @dagger.Component$Builder class **.*$*
-keep class <1>.Dagger<2> {
  static ** builder();
}

# For each component factory type that is kept, keep the name of that type which will ensure it
# remains nested inside its corresponding component type.
-if @dagger.Component$Factory class **.*$*
-keep class <1>.<2>

# For each component factory type, keep the corresponding Dagger-generated component type and the
# factory method for the factory.
-if @dagger.Component$Factory class **.*$*
-keep class <1>.Dagger<2> {
  static ** factory();
}
