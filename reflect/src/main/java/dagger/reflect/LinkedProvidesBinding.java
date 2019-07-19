package dagger.reflect;

import static dagger.reflect.Reflection.tryInvoke;

import dagger.reflect.Binding.LinkedBinding;
import java.lang.reflect.Method;
import org.jetbrains.annotations.Nullable;

public final class LinkedProvidesBinding<T> extends LinkedBinding<T> {
  private final @Nullable Object instance;
  private final Method method;
  private final LinkedBinding<?>[] dependencies;
  private final boolean nullable;

  LinkedProvidesBinding(
      @Nullable Object instance, Method method, LinkedBinding<?>[] dependencies, boolean nullable) {
    this.instance = instance;
    this.method = method;
    this.dependencies = dependencies;
    this.nullable = nullable;
  }

  @Override
  public @Nullable T get() {
    Object[] arguments = new Object[dependencies.length];
    for (int i = 0; i < arguments.length; i++) {
      arguments[i] = dependencies[i].get();
    }
    // The binding is associated with the return type of method as key.
    @SuppressWarnings("unchecked")
    T value = (T) tryInvoke(instance, method, arguments);
    // We could add a check to ensure that value is null only if this Binding is annotated with
    // nullable. However, we cannot because not all Nullable annotations have runtime retention so
    // it would return false positives.
    return value;
  }

  boolean nullableMatch(boolean nullable) {
    if (this.nullable && !nullable) {
      return false;
    }
    if (!this.nullable && nullable) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    String nullableString = nullable ? "@Nullable " : "";
    return nullableString
        + "@Provides["
        + method.getDeclaringClass().getName()
        + '.'
        + method.getName()
        + "(…)]";
  }
}
