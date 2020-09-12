package dagger.reflect;

import dagger.MembersInjector;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import javax.annotation.Nullable;

final class MembersInjectorJustInTimeFactory implements JustInTimeLookup.Factory {
  @Override
  public @Nullable JustInTimeLookup create(Key key) {
    Type type = key.type();
    if (key.qualifier() != null || !(type instanceof ParameterizedType)) {
      return null;
    }
    ParameterizedType parameterizedType = ((ParameterizedType) type);
    Type rawType = parameterizedType.getRawType();
    if (rawType != MembersInjector.class) {
      return null;
    }
    Type targetType = parameterizedType.getActualTypeArguments()[0];
    if (targetType instanceof ParameterizedType) {
      ParameterizedType innerType = (ParameterizedType) targetType;
      if (innerType.getActualTypeArguments()[0] instanceof WildcardType) {
        throw new IllegalStateException(
            "Cannot inject members into types with unbounded type arguments: " + key);
      }
      // TODO what do we do here? turn Foo<Bar> into Foo.class? What if an injected type is the T?
      throw new UnsupportedOperationException("TODO");
    }
    Class<?> targetClass = ((Class<?>) targetType);
    return new JustInTimeLookup(null, new UnlinkedMembersInjectorBinding(targetClass));
  }
}
