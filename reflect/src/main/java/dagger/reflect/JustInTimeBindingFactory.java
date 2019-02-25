package dagger.reflect;

import dagger.reflect.Binding.UnlinkedBinding;
import org.jetbrains.annotations.Nullable;

interface JustInTimeBindingFactory {
  @Nullable UnlinkedBinding create(Key key);
}
