package dagger.reflect;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import javax.inject.Inject;
import org.jetbrains.annotations.Nullable;

import static dagger.reflect.DaggerReflect.notImplemented;
import static dagger.reflect.Reflection.findScope;

final class ReflectiveJustInTimeProvider implements BindingGraph.JustInTimeProvider {
  @Override public @Nullable Binding<?> create(Key key) {
    Annotation qualifier = key.qualifier();
    if (qualifier != null) {
      return null; // Qualified types can't be just-in-time satisfied.
    }

    Type type = key.type();
    if (!(type instanceof Class<?>)) {
      return null; // Parameterized/array types can't be just-in-time satisfied.
    }
    return create((Class<?>) type);
  }

  private static <T> @Nullable Binding<T> create(Class<T> cls) {
    Annotation scope = findScope(cls.getAnnotations());
    if (scope != null) {
      throw notImplemented("Just-in-time scoped bindings");
    }

    @SuppressWarnings("unchecked")
    Constructor<T>[] constructors = (Constructor<T>[]) cls.getDeclaredConstructors();
    Constructor<T> target = null;
    for (Constructor<T> constructor : constructors) {
      if (constructor.getAnnotation(Inject.class) != null) {
        if (target != null) {
          throw new IllegalStateException(
              cls.getCanonicalName() + " defines multiple @Inject-annotations constructors");
        }
        target = constructor;
      }
    }
    if (target == null) {
      return null; // Types without an @Inject constructor cannot be just-in-time satisfied.
    }

    return new Binding.UnlinkedJustInTime<>(cls, target);
  }
}
