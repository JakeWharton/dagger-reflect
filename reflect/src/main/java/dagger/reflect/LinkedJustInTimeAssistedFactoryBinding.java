package dagger.reflect;

import static dagger.reflect.Reflection.tryInstantiate;

import dagger.MembersInjector;
import dagger.reflect.Binding.LinkedBinding;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

public final class LinkedJustInTimeAssistedFactoryBinding<T> extends LinkedBinding<T>
    implements InvocationHandler {
  private final Class<T> factory;
  private final Constructor<?> constructor;
  private final MembersInjector<?> membersInjector;

  /** Non-Assisted parameters are filled with null */
  private final Integer[] parameterToAssistedArgumentIndexes;

  /** Assisted parameters are filled with null */
  private final LinkedBinding<?>[] dependencies;

  LinkedJustInTimeAssistedFactoryBinding(
      Class<T> factory,
      Constructor<?> constructor,
      MembersInjector<?> membersInjector,
      LinkedBinding<?>[] dependencies,
      Integer[] parameterToAssistedArgumentIndexes) {
    this.factory = factory;
    this.constructor = constructor;
    this.membersInjector = membersInjector;
    this.dependencies = dependencies;
    this.parameterToAssistedArgumentIndexes = parameterToAssistedArgumentIndexes;
  }

  @Override
  public T get() {
    return Reflection.newProxy(factory, this);
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) {
    Object[] arguments = new Object[dependencies.length];

    for (int i = 0; i < dependencies.length; i++) {
      // Either of those should not be null
      Integer assistedArgumentIndex = parameterToAssistedArgumentIndexes[i];
      LinkedBinding<?> nonAssistedArgumentBinding = dependencies[i];

      if (assistedArgumentIndex != null) {
        arguments[i] = args[assistedArgumentIndex];
      } else {
        arguments[i] = nonAssistedArgumentBinding.get();
      }
    }

    Object instance = tryInstantiate(constructor, arguments);

    //noinspection unchecked
    ((MembersInjector<Object>) membersInjector).injectMembers(instance);

    return instance;
  }

  @Override
  public String toString() {
    return "@AssistedInject[" + constructor.getDeclaringClass().getName() + ".<init>(…)]";
  }
}
