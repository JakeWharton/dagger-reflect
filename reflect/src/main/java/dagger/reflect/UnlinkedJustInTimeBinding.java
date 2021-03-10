package dagger.reflect;

import static dagger.reflect.Reflection.findQualifier;

import dagger.MembersInjector;
import dagger.reflect.Binding.UnlinkedBinding;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

final class UnlinkedJustInTimeBinding<T> extends UnlinkedBinding {
  private final Class<T> cls;
  private final Constructor<T> constructor;
  private final ParameterTypesResolver<T> parameterTypesResolver;

  UnlinkedJustInTimeBinding(
      Class<T> cls, Constructor<T> constructor, ParameterTypesResolver<T> parameterTypesResolver) {
    this.cls = cls;
    this.constructor = constructor;
    this.parameterTypesResolver = parameterTypesResolver;
  }

  @Override
  public LinkedBinding<?> link(Linker linker, Scope scope) {
    Type[] parameterTypes = parameterTypesResolver.getActualParameterTypes();
    Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();

    LinkedBinding<?>[] bindings = new LinkedBinding<?>[parameterTypes.length];
    for (int i = 0; i < parameterTypes.length; i++) {
      Type parameterType = parameterTypes[i];
      Key key = Key.of(findQualifier(parameterAnnotations[i]), parameterType);
      bindings[i] = linker.get(key);
    }

    MembersInjector<T> membersInjector = ReflectiveMembersInjector.create(cls, scope);

    return new LinkedJustInTimeBinding<>(constructor, bindings, membersInjector);
  }

  @Override
  public String toString() {
    return "@Inject["
        + cls.getName()
        + parameterTypesResolver.getTypeArgumentsStringOrEmpty()
        + ".<init>(…)]";
  }
}
