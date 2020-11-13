package dagger.reflect;

import static dagger.reflect.Reflection.findQualifier;

import dagger.MembersInjector;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

final class UnlinkedJustInTimeContributesAndroidInjectorBinding<T> extends Binding.UnlinkedBinding {
  private final Class<T> cls;
  private final Constructor<T> constructor;
  // Type arguments might be used as types for this binding's parameterized constructor parameters.
  private @Nullable Type[] concreteTypeArguments;

  UnlinkedJustInTimeContributesAndroidInjectorBinding(
      Class<T> cls, Constructor<T> constructor, @Nullable Type[] concreteTypeArguments) {
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
      Key key = Key.of(findQualifier(parameterAnnotations[i]), parameterType);
      bindings[i] = linker.get(key);
      addParentMappings(key, scope.getParent(), linker);
    }

    MembersInjector<T> membersInjector = ReflectiveMembersInjector.create(cls, scope);

    return new LinkedJustInTimeBinding<>(constructor, bindings, membersInjector);
  }

  private void addParentMappings(Key key, @Nullable Scope parentScope, Linker linker) {
    if (parentScope == null) {
      return;
    }

    Binding.LinkedBinding<?> bindingLinked = linker.get(key);
    Object value = bindingLinked.get();

    if (value instanceof Map) {
      Map<Class<?>, Binding> bindingValue = (Map<Class<?>, Binding>) value;
      Binding.LinkedBinding<?> parentBinding = parentScope.getBinding(key);
      if (parentBinding != null) {
        Object parentBindingValue = parentBinding.get();
        if (parentBindingValue instanceof Map) {
          Map<Class<?>, LinkedBinding> parentBindingValueMap =
              (Map<Class<?>, LinkedBinding>) parentBindingValue;
          bindingValue.putAll(parentBindingValueMap);
        }
      }
    }

    addParentMappings(key, parentScope.getParent(), linker);
  }

  @Override
  public String toString() {
    return "@Inject[" + cls.getName() + getTypeArgumentsStringOrEmpty() + ".<init>(…)]";
  }

  private String getTypeArgumentsStringOrEmpty() {
    if (concreteTypeArguments == null) {
      return "";
    }
    return Arrays.toString(concreteTypeArguments);
  }
}
