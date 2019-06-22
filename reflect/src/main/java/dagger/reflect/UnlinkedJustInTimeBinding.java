package dagger.reflect;

import static dagger.reflect.Reflection.findQualifier;

import dagger.MembersInjector;
import dagger.reflect.Binding.UnlinkedBinding;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;

final class UnlinkedJustInTimeBinding<T> extends UnlinkedBinding {
  private final Class<T> cls;
  private final Constructor<T> constructor;
  // Type arguments might be used as types for this binding's parameterized constructor parameters.
  @Nullable private Type[] concreteTypeArguments;

  UnlinkedJustInTimeBinding(Class<T> cls, Constructor<T> constructor, @Nullable Type[] concreteTypeArguments) {
    this.cls = cls;
    this.constructor = constructor;
    this.concreteTypeArguments = concreteTypeArguments;
  }

  @Override
  public LinkedBinding<?> link(Linker linker, Scope scope) {
    Type[] parameterTypes = constructor.getGenericParameterTypes();
    Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();

    LinkedBinding<?>[] bindings = new LinkedBinding<?>[parameterTypes.length];
    for (int i = 0; i < parameterTypes.length; i++) {
      Type parameterType = parameterTypes[i];
      Key key =
          Key.of(findQualifier(parameterAnnotations[i]), getTypeKeyForParameter(parameterType));
      bindings[i] = linker.get(key);
    }

    MembersInjector<T> membersInjector = ReflectiveMembersInjector.create(cls, scope);

    return new LinkedJustInTimeBinding<>(constructor, bindings, membersInjector);
  }

  private Type getTypeKeyForParameter(Type parameterType) {
    if (parameterType instanceof ParameterizedType) {
      return findKeyForParameterizedType((ParameterizedType) parameterType);
    } else {
      return parameterType;
    }
  }

  private TypeUtil.ParameterizedTypeImpl findKeyForParameterizedType(
      ParameterizedType parameterType) {
    Type matchingType = matchingParameterizedType(parameterType);
    return new TypeUtil.ParameterizedTypeImpl(null, parameterType.getRawType(), matchingType);
  }

  /**
   * Given a parameterized type `Provider<T>` where `T` is our type parameter placeholder: We first
   * look at this class's parameterized type declarations to see if we can find a matching `T`. When
   * we find a matching `T`, we use `T`'s index to find the corresponding concrete type argument in
   * this binding's concreteTypeArguments.
   *
   * @param parameterizedType The parameterized type placeholder to lookup.
   * @return The matching concrete type for the placeholder.
   */
  private Type matchingParameterizedType(ParameterizedType parameterizedType) {
    Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
    if (actualTypeArguments.length > 1) {
      throw new IllegalStateException(
          "Multiple parameterized type arguments are not supported yet.");
    }
    Type typeToLookup = actualTypeArguments[0];
    // If this isn't a TypeVariable like <T> than we can just return the parameterized type.
    if (!(typeToLookup instanceof TypeVariable)) {
      return parameterizedType;
    }
    if (concreteTypeArguments == null) {
      throw new IllegalStateException(
          "No concrete type arguments for " + cls + " but needed for " + parameterizedType);
    }
    // Iterate through parameterized types declared in this class to find the matching index and
    // return the corresponding concrete type.
    for (int i = 0; i < cls.getTypeParameters().length; i++) {
      if (cls.getTypeParameters()[i].equals(typeToLookup)) {
        return concreteTypeArguments[i];
      }
    }
    throw new IllegalStateException("Could not finding matching parameterized type arguments for "
        + parameterizedType
        + " in "
        + Arrays.toString(cls.getTypeParameters()));
  }

  @Override
  public String toString() {
    return "@Inject[" + cls.getName() + getTypeArgumentsStringOrEmpty() +".<init>(…)]";
  }

  private String getTypeArgumentsStringOrEmpty() {
    if (concreteTypeArguments == null) {
      return "";
    }
    return Arrays.toString(concreteTypeArguments);
  }
}
