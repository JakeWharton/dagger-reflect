package dagger.reflect;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import org.jetbrains.annotations.Nullable;

/** Resolves actual types of generic parameters of method/constructor */
final class ParameterTypesResolver<T> {
  private final Class<T> cls;
  private @Nullable Type[] concreteTypeArguments;
  /** type arguments of cls method/constructor from getGenericParameterTypes() */
  private final Type[] parameterTypes;

  private ParameterTypesResolver(
      Class<T> cls, Type[] parameterTypes, @Nullable Type[] concreteTypeArguments) {
    this.cls = cls;
    this.parameterTypes = parameterTypes;
    this.concreteTypeArguments = concreteTypeArguments;
  }

  public static <T> ParameterTypesResolver<T> ofConstructor(
      Type type, Constructor<T> typeConstructor) {
    return ofExecutable(type, typeConstructor.getGenericParameterTypes());
  }

  public static <T> ParameterTypesResolver<T> ofMethod(Type type, Method typeMethod) {
    return ofExecutable(type, typeMethod.getGenericParameterTypes());
  }

  private static <T> ParameterTypesResolver<T> ofExecutable(
      Type type, Type[] executableParameterTypes) {

    Type[] typeArguments = null;
    if (type instanceof ParameterizedType) {
      typeArguments = ((ParameterizedType) type).getActualTypeArguments();
    }

    Class<T> cls = Types.getRawClassOrInterface(type);

    if (cls == null) {
      throw new IllegalStateException("Arrays are not supported");
    }

    return new ParameterTypesResolver<>(cls, executableParameterTypes, typeArguments);
  }

  public Type[] getActualParameterTypes() {
    Type[] types = new Type[parameterTypes.length];
    for (int i = 0; i < parameterTypes.length; i++) {
      Type parameterType = parameterTypes[i];
      types[i] = getTypeKeyForParameter(parameterType);
    }

    // TODO cache this?
    return types;
  }

  private Type getTypeKeyForParameter(Type parameterType) {
    if (isTypeVariable(parameterType)) {
      return matchTypeToConcreteType((TypeVariable<?>) parameterType);
    } else if (hasParameterizedTypeVariable(parameterType)) {
      return findKeyForParameterizedType((ParameterizedType) parameterType);
    }
    return parameterType;
  }

  private static boolean isTypeVariable(Type parameterType) {
    return parameterType instanceof TypeVariable;
  }

  private static boolean hasParameterizedTypeVariable(Type parameterType) {
    if (!(parameterType instanceof ParameterizedType)) {
      return false;
    }
    Type[] actualTypeArguments = ((ParameterizedType) parameterType).getActualTypeArguments();
    for (Type type : actualTypeArguments) {
      if (isTypeVariable(type)) {
        return true;
      }
    }
    return false;
  }

  private TypeUtil.ParameterizedTypeImpl findKeyForParameterizedType(
      ParameterizedType parameterType) {
    Type[] matchingTypes = matchingParameterizedType(parameterType.getActualTypeArguments());
    return new TypeUtil.ParameterizedTypeImpl(null, parameterType.getRawType(), matchingTypes);
  }

  /**
   * Find matching concrete types for a list of types. For every TypeVariable like `T` in the array
   * arg, we lookup the matching type in this class's concrete type arguments. If it is already a
   * concrete type, just return the type. Creates a new array to match parameterizd type.
   *
   * @param typeArguments The Types and TypeVariables to find matching concrete types for.
   * @return The matching concrete type for the placeholder.
   */
  private Type[] matchingParameterizedType(Type[] typeArguments) {
    Type[] matchedTypeArguments = new Type[typeArguments.length];
    for (int i = 0; i < typeArguments.length; i++) {
      if (isTypeVariable(typeArguments[i])) {
        matchedTypeArguments[i] = matchTypeToConcreteType((TypeVariable<?>) typeArguments[i]);
      } else {
        matchedTypeArguments[i] = typeArguments[i];
      }
    }
    return matchedTypeArguments;
  }

  /**
   * Given a TypeVariable `T`, we look at this class's parameterized type declarations to see if we
   * can find a matching `T`. When we find a matching `T`, we use `T`'s index to find the
   * corresponding concrete type argument in this binding's concreteTypeArguments.
   *
   * @param typeToLookup The parameterized type placeholder to lookup.
   * @return The matching concrete type for the placeholder.
   */
  private Type matchTypeToConcreteType(TypeVariable<?> typeToLookup) {
    if (concreteTypeArguments == null) {
      throw new IllegalStateException(
          "No concrete type arguments for " + cls + " but needed for " + typeToLookup);
    }
    // Iterate through parameterized types declared in this class to find the matching index and
    // return the corresponding concrete type.
    TypeVariable<Class<T>>[] typeParameters = cls.getTypeParameters();
    for (int i = 0, length = typeParameters.length; i < length; i++) {
      if (typeParameters[i].equals(typeToLookup)) {
        return concreteTypeArguments[i];
      }
    }
    throw new IllegalStateException(
        "Could not finding matching parameterized type arguments for "
            + typeToLookup
            + " in "
            + Arrays.toString(typeParameters));
  }

  String getTypeArgumentsStringOrEmpty() {
    if (concreteTypeArguments == null) {
      return "";
    }
    return Arrays.toString(concreteTypeArguments);
  }
}
