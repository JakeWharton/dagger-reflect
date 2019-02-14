package dagger.reflect;

import dagger.reflect.Binding.UnlinkedBinding;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import javax.inject.Inject;
import org.jetbrains.annotations.Nullable;

import static dagger.reflect.DaggerReflect.notImplemented;
import static dagger.reflect.Reflection.findScope;

final class ReflectiveJustInTimeProvider implements BindingMap.JustInTimeProvider {
  @Override public @Nullable UnlinkedBinding create(Key key) {
    Annotation qualifier = key.qualifier();
    if (qualifier != null) {
      return null; // Qualified types can't be just-in-time satisfied.
    }

    Type type = key.type();
    Class<Object> cls;
    if (type instanceof ParameterizedType) {
      cls = (Class<Object>) ((ParameterizedType) type).getRawType();
    } else if (type instanceof Class<?>) {
      cls = (Class<Object>) type;
    } else {
      return null; // Array types can't be just-in-time satisfied.
    }

    Annotation scope = findScope(cls.getAnnotations());
    if (scope != null) {
      throw notImplemented("Just-in-time scoped bindings");
    }

    Constructor<?>[] constructors = cls.getDeclaredConstructors();
    Constructor<Object> target = null;
    for (Constructor<?> constructor : constructors) {
      if (constructor.getAnnotation(Inject.class) != null) {
        if (target != null) {
          throw new IllegalStateException(
              cls.getCanonicalName() + " defines multiple @Inject-annotations constructors");
        }
        target = (Constructor<Object>) constructor;
      }
    }
    if (target == null) {
      return null; // Types without an @Inject constructor cannot be just-in-time satisfied.
    }

    return new UnlinkedJustInTimeBinding<>(cls, target);
  }
}
