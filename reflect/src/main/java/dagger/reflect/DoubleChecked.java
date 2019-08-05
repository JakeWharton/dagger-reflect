package dagger.reflect;

import dagger.Lazy;
import javax.inject.Provider;
import org.jetbrains.annotations.Nullable;

abstract class DoubleChecked<T> implements Lazy<T>, Provider<T> {
  private static final Object UNINITIALIZED = new Object();

  private volatile @Nullable Object value = UNINITIALIZED;

  @SuppressWarnings("unchecked") // Value from getOnce() by the time the unchecked cast is reached.
  @Override
  public @Nullable T get() {
    Object value = this.value;
    if (value == UNINITIALIZED) {
      synchronized (this) {
        value = this.value;
        if (value == UNINITIALIZED) {
          value = this.value = compute();
        }
      }
    }
    return (T) value;
  }

  abstract @Nullable T compute();
}
