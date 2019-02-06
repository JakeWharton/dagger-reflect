package dagger.reflect;

import dagger.reflect.Binding.LinkedBinding;
import java.util.Optional;
import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement;
import org.jetbrains.annotations.Nullable;

@IgnoreJRERequirement // Only linked when requested by user code referencing j.u.Optional.
final class LinkedJavaOptionalBinding<T> extends LinkedBinding<Optional<T>> {
  private final @Nullable LinkedBinding<T> dependency;

  LinkedJavaOptionalBinding(@Nullable LinkedBinding<T> dependency) {
    this.dependency = dependency;
  }

  @Override
  public Optional<T> get() {
    if (dependency == null) {
      return Optional.empty();
    }
    T value = dependency.get();
    if (value == null) {
      throw new NullPointerException(
          dependency + " returned null which is not allowed for optional bindings");
    }
    return Optional.of(value);
  }

  @Override
  public String toString() {
    return "@BindsOptionalOf[" + dependency + ']';
  }
}
