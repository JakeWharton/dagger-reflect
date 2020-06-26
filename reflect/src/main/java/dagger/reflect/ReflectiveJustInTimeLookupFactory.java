package dagger.reflect;

import static dagger.reflect.Reflection.findScope;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import javax.inject.Inject;
import org.jetbrains.annotations.Nullable;

final class ReflectiveJustInTimeLookupFactory implements JustInTimeLookup.Factory {
  @Override
  public @Nullable JustInTimeLookup create(Key key) {
    if (key.qualifier() != null) {
      return null; // Qualified types can't be just-in-time satisfied.
    }

    Type type = key.type();
    return getJustInTimeLookup(type);
  }

  private @Nullable <T> JustInTimeLookup getJustInTimeLookup(Type type) {
    Class<T> cls;
    Type[] typeArguments = null;
    if (type instanceof ParameterizedType) {
      // Assume that "representing the class or interface that declared this type" is a Class<?>.
      @SuppressWarnings("unchecked")
      Class<T> rawType = (Class<T>) ((ParameterizedType) type).getRawType();
      cls = rawType;
      typeArguments = ((ParameterizedType) type).getActualTypeArguments();
    } else if (type instanceof Class<?>) {
      @SuppressWarnings("unchecked")
      Class<T> directClass = (Class<T>) type;
      cls = directClass;
    } else {
      return null; // Array types can't be just-in-time satisfied.
    }

    Constructor<T> target = findSingleInjectConstructor(cls);
    if (target == null) {
      return null; // Types without an @Inject constructor cannot be just-in-time satisfied.
    }

    Annotation scope = findScope(cls.getAnnotations());
    Binding binding;
    if (TypeUtil.isAndroidInjectorFactory(type)) {
      binding = new UnlinkedJustInTimeContributesAndroidInjectorBinding<>(cls, target, typeArguments);
    } else {
      binding = new UnlinkedJustInTimeBinding<>(cls, target, typeArguments);
    }
    return new JustInTimeLookup(scope, binding);
  }

  private static <T> @Nullable Constructor<T> findSingleInjectConstructor(Class<T> cls) {
    // Not modifying it, safe to use generics; see Class#getConstructors() for more info.
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
    return target;
  }
}
