package com.example;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;

import dagger.Lazy;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.inject.Provider;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public final class IntegrationTest {
  @Parameters(name = "{0}")
  public static Object[] parameters() {
    return Backend.values();
  }

  private final Backend backend;
  @Rule public final TestRule rule;

  public IntegrationTest(Backend backend) {
    this.backend = backend;
    this.rule = new IntegrationTestRule(backend);
  }

  @Test
  public void componentProvider() {
    ComponentProvider component = backend.create(ComponentProvider.class);
    assertThat(component.string()).isEqualTo("foo");
  }

  @Test
  public void componentProviderNull() {
    ComponentProviderNull component = backend.create(ComponentProviderNull.class);
    assertThat(component.string()).isNull();
  }

  @Test
  public void componentProviderQualified() {
    ComponentProviderQualified component = backend.create(ComponentProviderQualified.class);
    assertThat(component.string()).isEqualTo("foo");
  }

  @Test
  public void staticProvider() {
    StaticProvider component = backend.create(StaticProvider.class);
    assertThat(component.string()).isEqualTo("foo");
  }

  @Test
  public void bindsProvider() {
    BindsProvider component = backend.create(BindsProvider.class);
    assertThat(component.number()).isEqualTo(42);
  }

  @Test
  @IgnoreCodegen
  @ReflectBug("check not implemented")
  public void bindsProviderNullabilityMismatch() {
    BindsProviderNullabilityMismatch component =
        backend.create(BindsProviderNullabilityMismatch.class);
    try {
      assertThat(component.string()).isNull();
      fail();
    } catch (Exception e) {
      // TODO assert some error message similar to "java.lang.String is not nullable, but is being
      // provided by @Provides @Nullable String
      // com.example.BindsProviderNull.Module1.provideString()"
    }
  }

  @Test
  public void bindsProviderNull() {
    BindsProviderNull component = backend.create(BindsProviderNull.class);
    assertThat(component.string()).isNull();
  }

  @Test
  public void bindIntoSet() {
    BindsIntoSet component = backend.create(BindsIntoSet.class);
    assertThat(component.strings()).containsExactly("foo");
  }

  @Test
  public void bindElementsIntoSet() {
    BindsElementsIntoSet component = backend.create(BindsElementsIntoSet.class);
    assertThat(component.strings()).containsExactly("foo");
  }

  @Test
  @IgnoreCodegen
  public void bindElementsIntoSetWrongReturn() {
    try {
      backend.create(BindsElementsIntoSetWrongReturn.class);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo("@BindsIntoSet must return Set. Found class java.lang.String.");
    }
  }

  @Test
  @IgnoreCodegen
  public void bindElementsIntoSetGenericWrongReturn() {
    try {
      backend.create(BindsElementsIntoSetGenericWrongReturn.class);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "@Binds methods must return a primitive, an array, a type variable, or a "
                  + "declared type. Found java.util.Set<? extends java.lang.String>.");
    }
  }

  @Test
  public void bindIntoMap() {
    BindsIntoMap component = backend.create(BindsIntoMap.class);
    assertThat(component.strings()).containsExactly("bar", "foo");
  }

  @Test
  public void mapWithoutBinds() {
    MapWithoutBinds component = backend.create(MapWithoutBinds.class);
    assertThat(component.strings()).containsExactly("1", "one", "2", "two");
  }

  @Test
  public void mapProviderWithoutBinds() {
    MapProviderWithoutBinds component = backend.create(MapProviderWithoutBinds.class);

    Map<String, Provider<String>> values = component.strings();
    assertThat(values.keySet()).containsExactly("1", "2");

    // Ensure each Provider is lazy in invoking its backing @Provides method.
    MapProviderWithoutBinds.Module1.twoValue.set("two");
    assertThat(values.get("2").get()).isEqualTo("two");

    MapProviderWithoutBinds.Module1.oneValue.set("one");
    assertThat(values.get("1").get()).isEqualTo("one");
  }

  @Test
  public void mapLazyWithoutBinds() {
    MapLazyWithoutBinds component = backend.create(MapLazyWithoutBinds.class);

    Map<String, Lazy<String>> values = component.strings();
    assertThat(values.keySet()).containsExactly("1", "2");

    // Ensure each Provider is lazy in invoking its backing @Provides method.
    MapLazyWithoutBinds.Module1.twoValue.set("two");
    assertThat(values.get("2").get()).isEqualTo("two");

    MapLazyWithoutBinds.Module1.oneValue.set("one");
    assertThat(values.get("1").get()).isEqualTo("one");
  }

  @Test
  public void optionalBinding() {
    OptionalBinding component = backend.create(OptionalBinding.class);
    assertThat(component.string()).isEqualTo(Optional.of("foo"));
  }

  @Test
  @IgnoreCodegen
  public void optionalBindingNullable() {
    OptionalBindingNullable component = backend.create(OptionalBindingNullable.class);
    try {
      component.string();
      fail();
    } catch (NullPointerException e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "@Provides[com.example.OptionalBindingNullable$Module1.foo(…)] "
                  + "returned null which is not allowed for optional bindings");
    }
  }

  @Test
  public void optionalBindingAbsent() {
    OptionalBindingAbsent component = backend.create(OptionalBindingAbsent.class);
    assertThat(component.string()).isEqualTo(Optional.empty());
  }

  @Test
  public void optionalBindingPrimitive() {
    OptionalBindingPrimitive component = backend.create(OptionalBindingPrimitive.class);
    assertThat(component.five()).isEqualTo(Optional.of(5L));
  }

  @SuppressWarnings("Guava") // Explicitly testing Guava support.
  @Test
  public void optionalGuavaBinding() {
    OptionalGuavaBinding component = backend.create(OptionalGuavaBinding.class);
    assertThat(component.string()).isEqualTo(com.google.common.base.Optional.of("foo"));
  }

  @Test
  public void optionalGuavaBindingAbsent() {
    OptionalGuavaBindingAbsent component = backend.create(OptionalGuavaBindingAbsent.class);
    assertThat(component.string()).isEqualTo(com.google.common.base.Optional.absent());
  }

  @SuppressWarnings("Guava") // Explicitly testing Guava support.
  @Test
  public void optionalGuavaBindingPrimitive() {
    OptionalGuavaBindingPrimitive component = backend.create(OptionalGuavaBindingPrimitive.class);
    assertThat(component.five()).isEqualTo(com.google.common.base.Optional.of(5L));
  }

  @Test
  public void justInTimeConstructor() {
    JustInTimeConstructor component = backend.create(JustInTimeConstructor.class);
    assertThat(component.thing()).isNotNull();
  }

  @Test
  public void justInTimeGeneric() {
    JustInTimeGeneric component = backend.create(JustInTimeGeneric.class);
    assertThat(component.thing()).isNotNull();
  }

  @Test
  public void justInTimeMembersInjection() {
    JustInTimeMembersInjection component = backend.create(JustInTimeMembersInjection.class);
    JustInTimeMembersInjection.Thing thing = component.thing();
    assertThat(thing.stringConstructor).isEqualTo("hey");
    assertThat(thing.stringField).isEqualTo("hey");
    assertThat(thing.stringMethod).isEqualTo("hey");
  }

  @Test
  public void justInTimeScoped() {
    JustInTimeScoped component = backend.create(JustInTimeScoped.class);
    JustInTimeScoped.Thing thing1 = component.thing();
    JustInTimeScoped.Thing thing2 = component.thing();
    assertThat(thing1).isSameInstanceAs(thing2);
  }

  @Test
  public void justInTimeScopedInParent() {
    JustInTimeScopedInParent component = backend.create(JustInTimeScopedInParent.class);
    JustInTimeScopedInParent.ChildComponent child1 = component.child();
    JustInTimeScopedInParent.Thing thing1 = child1.thing();
    JustInTimeScopedInParent.ChildComponent child2 = component.child();
    JustInTimeScopedInParent.Thing thing2 = child2.thing();
    assertThat(thing1).isSameInstanceAs(thing2);
  }

  @Test
  public void justInTimeUnscopedIntoJustInTimeScoped() {
    JustInTimeDependsOnJustInTime component = backend.create(JustInTimeDependsOnJustInTime.class);
    JustInTimeDependsOnJustInTime.Foo foo1 = component.thing();
    JustInTimeDependsOnJustInTime.Foo foo2 = component.thing();
    assertThat(foo1).isNotSameInstanceAs(foo2);
  }

  @Test
  @IgnoreCodegen
  public void justInTimeWrongScope() {
    JustInTimeWrongScope component = backend.create(JustInTimeWrongScope.class);
    try {
      component.thing();
      fail();
    } catch (IllegalStateException e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Unable to find binding for key=com.example.JustInTimeWrongScope$Thing"
                  + " with linker=null");
    }
  }

  @Test
  @IgnoreCodegen
  public void justInTimeScopedIntoUnscoped() {
    JustInTimeScopedIntoUnscoped component = backend.create(JustInTimeScopedIntoUnscoped.class);
    try {
      component.thing();
      fail();
    } catch (IllegalStateException e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Unable to find binding for key=com.example.JustInTimeScopedIntoUnscoped$Thing"
                  + " with linker=null");
    }
  }

  @Test
  @IgnoreCodegen
  public void justInTimeNotScopedInAncestry() {
    JustInTimeNotScopedInAncestry.ChildComponent child =
        backend.create(JustInTimeNotScopedInAncestry.class).child();
    try {
      child.thing();
      fail();
    } catch (IllegalStateException e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Unable to find binding for key=com.example.JustInTimeNotScopedInAncestry$Thing"
                  + " with linker=null");
    }
  }

  @Test
  public void providerGenericIntoJustInTimeGeneric() {
    ProviderGenericIntoJustInTime component = backend.create(ProviderGenericIntoJustInTime.class);
    assertThat(component.thing().genericProvider.get()).isNotNull();
  }

  @Test
  public void providerMultipleGenericIntoJustInTimeGeneric() {
    ProviderMultipleGenericIntoJustInTime component =
        backend.create(ProviderMultipleGenericIntoJustInTime.class);
    assertThat(component.thing().thingProvider.get()).isNotNull();
    assertThat(component.thing().valueProvider.get()).isNotNull();
  }

  @Test
  public void providerUnscopedBinding() {
    ProviderUnscopedBinding component = backend.create(ProviderUnscopedBinding.class);
    Provider<String> value = component.value();

    // Ensure the Provider is lazy in invoking and aggregating its backing @Provides methods.
    ProviderUnscopedBinding.Module1.oneCount.set(1);

    assertThat(value.get()).isEqualTo("one1");
    assertThat(value.get()).isEqualTo("one2");
  }

  @Test
  public void providerScopedBinding() {
    ProviderScopedBinding component = backend.create(ProviderScopedBinding.class);
    Provider<String> value = component.value();

    // Ensure the Provider is lazy in invoking and aggregating its backing @Provides methods.
    ProviderScopedBinding.Module1.oneCount.set(1);

    assertThat(value.get()).isSameInstanceAs(value.get());
    assertThat(value.get()).isEqualTo("one1");
    assertThat(value.get()).isEqualTo("one1");
  }

  @Test
  public void lazyInvokedTwiceInstancesAreSame() {
    LazyUnscopedBinding component = backend.create(LazyUnscopedBinding.class);
    Lazy<String> value = component.value();

    // Ensure the Provider is lazy in invoking and aggregating its backing @Provides methods.
    LazyUnscopedBinding.Module1.oneCount.set(1);

    assertThat(value.get()).isEqualTo("one1");
    assertThat(value.get()).isEqualTo("one1");
  }

  @Test
  public void lazyInvokedTwiceDifferentLazyDifferentInstances() {
    LazyUnscopedBinding component = backend.create(LazyUnscopedBinding.class);
    Lazy<String> lazyOne = component.value();
    Lazy<String> lazyTwo = component.value();

    // Ensure the Provider is lazy in invoking and aggregating its backing @Provides methods.
    LazyUnscopedBinding.Module1.oneCount.set(1);

    assertThat(lazyOne.get()).isEqualTo("one1");
    assertThat(lazyTwo.get()).isEqualTo("one2");
  }

  @Test
  public void lazyScopedInjection() {
    LazyScopedBinding component = backend.create(LazyScopedBinding.class);
    Lazy<String> value = component.value();

    // Ensure the Provider is lazy in invoking and aggregating its backing @Provides methods.
    LazyScopedBinding.Module1.oneCount.set(1);

    assertThat(value.get()).isEqualTo("one1");
    assertThat(value.get()).isEqualTo("one1");
  }

  @Test
  public void implicitModuleInstance() {
    ImplicitModuleInstance component = backend.create(ImplicitModuleInstance.class);

    assertThat(component.string()).isEqualTo("one");
  }

  @Test
  public void implicitModuleInstanceNotCreatedWhenUnnecessary() {
    ImplicitModuleInstanceCannotBeCreated component =
        backend.create(ImplicitModuleInstanceCannotBeCreated.class);

    assertThat(component.string()).isEqualTo("one");
  }

  @Test
  public void builderBindsInstance() {
    BuilderBindsInstance component =
        backend.builder(BuilderBindsInstance.Builder.class).string("foo").build();
    assertThat(component.string()).isEqualTo("foo");
  }

  @Test
  public void builderBindsInstanceCalledTwice() {
    BuilderBindsInstance component =
        backend.builder(BuilderBindsInstance.Builder.class).string("foo").string("bar").build();
    assertThat(component.string()).isEqualTo("bar");
  }

  @Test
  public void builderBindsInstanceNull() {
    BuilderBindsInstanceNull component =
        backend.builder(BuilderBindsInstanceNull.Builder.class).string(null).build();
    assertThat(component.string()).isNull();
  }

  @Test
  public void builderBindsInstanceOnParameter() {
    BuilderBindsInstanceOnParameter component =
        backend.builder(BuilderBindsInstanceOnParameter.Builder.class).string("foo").build();
    assertThat(component.string()).isEqualTo("foo");
  }

  @Test
  public void builderBindsInstanceOnParameterCalledTwice() {
    BuilderBindsInstanceOnParameter component =
        backend
            .builder(BuilderBindsInstanceOnParameter.Builder.class)
            .string("foo")
            .string("bar")
            .build();
    assertThat(component.string()).isEqualTo("bar");
  }

  @Test
  public void builderBindsInstanceOnParameterNull() {
    BuilderBindsInstanceOnParameterNull component =
        backend.builder(BuilderBindsInstanceOnParameterNull.Builder.class).string(null).build();
    assertThat(component.string()).isNull();
  }

  @Test
  @IgnoreCodegen
  public void builderBindsInstanceOnParameterAndMethod() {
    BuilderBindsInstanceOnParameterAndMethod.Builder builder =
        backend.builder(BuilderBindsInstanceOnParameterAndMethod.Builder.class);
    try {
      builder.string("hey");
      fail();
    } catch (IllegalStateException e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "@Component.Builder setter method "
                  + "com.example.BuilderBindsInstanceOnParameterAndMethod$Builder.string may not have "
                  + "@BindsInstance on both the method and its parameter; choose one or the other");
    }
  }

  @Test
  public void builderImplicitModules() {
    BuilderImplicitModules component =
        backend.builder(BuilderImplicitModules.Builder.class).value(3L).build();

    assertThat(component.string()).isEqualTo("3");
  }

  @Test
  public void builderExplicitModules() {
    BuilderExplicitModules component =
        backend
            .builder(BuilderExplicitModules.Builder.class)
            .module1(new BuilderExplicitModules.Module1("3"))
            .build();

    assertThat(component.string()).isEqualTo("3");
  }

  @Test
  public void builderExplicitModulesNullThrowsNpe() {
    BuilderExplicitModules.Builder builder = backend.builder(BuilderExplicitModules.Builder.class);
    try {
      builder.module1(null);
      fail();
    } catch (NullPointerException ignored) {
    }
  }

  @Test
  public void builderExplicitModulesSetTwice() {
    BuilderExplicitModules component =
        backend
            .builder(BuilderExplicitModules.Builder.class)
            .module1(new BuilderExplicitModules.Module1("3"))
            .module1(new BuilderExplicitModules.Module1("4"))
            .build();

    assertThat(component.string()).isEqualTo("4");
  }

  @Test
  public void builderExplicitModulesOmitted() {
    try {
      backend.builder(BuilderExplicitModules.Builder.class).build();
      fail();
    } catch (IllegalStateException e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo("com.example.BuilderExplicitModules.Module1 must be set");
    }
  }

  @Test
  public void builderDependency() {
    BuilderDependency component =
        backend
            .builder(BuilderDependency.Builder.class)
            .other(new BuilderDependency.Other("hey"))
            .build();

    assertThat(component.string()).isEqualTo("hey");
  }

  @Test
  public void builderDependencyNullThrowsNpe() {
    BuilderDependency.Builder builder = backend.builder(BuilderDependency.Builder.class);
    try {
      builder.other(null);
      fail();
    } catch (NullPointerException ignored) {
    }
  }

  @Test
  public void builderDependencySetTwice() {
    BuilderDependency component =
        backend
            .builder(BuilderDependency.Builder.class)
            .other(new BuilderDependency.Other("hey"))
            .other(new BuilderDependency.Other("there"))
            .build();

    assertThat(component.string()).isEqualTo("there");
  }

  @Test
  public void builderDependencyOmitted() {
    try {
      backend.builder(BuilderDependency.Builder.class).build();
      fail();
    } catch (IllegalStateException e) {
      assertThat(e).hasMessageThat().isEqualTo("com.example.BuilderDependency.Other must be set");
    }
  }

  @Test
  public void factoryBindsInstance() {
    FactoryBindsInstance component =
        backend.factory(FactoryBindsInstance.Factory.class).create("hey");

    assertThat(component.string()).isEqualTo("hey");
  }

  @Test
  public void factoryBindsInstanceNull() {
    FactoryBindsInstanceNull component =
        backend.factory(FactoryBindsInstanceNull.Factory.class).create(null);

    assertThat(component.string()).isNull();
  }

  @Test
  public void factoryDependency() {
    FactoryDependency component =
        backend.factory(FactoryDependency.Factory.class).create(new FactoryDependency.Other("hey"));

    assertThat(component.string()).isEqualTo("hey");
  }

  @Test
  public void factoryDependencyNullThrowsNpe() {
    FactoryDependency.Factory factory = backend.factory(FactoryDependency.Factory.class);
    try {
      factory.create(null);
      fail();
    } catch (NullPointerException ignored) {
    }
  }

  @Test
  public void factoryExplicitModules() {
    FactoryExplicitModules component =
        backend
            .factory(FactoryExplicitModules.Factory.class)
            .create(new FactoryExplicitModules.Module1("hey"));

    assertThat(component.string()).isEqualTo("hey");
  }

  @Test
  public void factoryExplicitModulesNullThrowsNpe() {
    FactoryExplicitModules.Factory factory = backend.factory(FactoryExplicitModules.Factory.class);
    try {
      factory.create(null);
      fail();
    } catch (NullPointerException ignored) {
    }
  }

  @Test
  public void factoryImplicitModules() {
    FactoryImplicitModules component =
        backend.factory(FactoryImplicitModules.Factory.class).create(3L);

    assertThat(component.string()).isEqualTo("3");
  }

  @Test
  public void memberInjectionEmptyClass() {
    MemberInjectionEmpty component = backend.create(MemberInjectionEmpty.class);
    MemberInjectionEmpty.Target target = new MemberInjectionEmpty.Target();
    component.inject(target);
    // No state, nothing to verify, except it didn't throw.
  }

  @Test
  public void memberInjectionEmptyAbstractClass() {
    MemberInjectionEmptyAbstract component = backend.create(MemberInjectionEmptyAbstract.class);
    MemberInjectionEmptyAbstract.Target target = new MemberInjectionEmptyAbstract.Target() {};
    component.inject(target);
    // No state, nothing to verify, except it didn't throw.
  }

  @Test
  public void memberInjectionEmptyInterface() {
    MemberInjectionEmptyInterface component = backend.create(MemberInjectionEmptyInterface.class);
    MemberInjectionEmptyInterface.Target target = new MemberInjectionEmptyInterface.Target() {};
    component.inject(target);
    // No state, nothing to verify, except it didn't throw.
  }

  @Test
  public void memberInjectionInterface() {
    MemberInjectionInterface component = backend.create(MemberInjectionInterface.class);
    class Target implements MemberInjectionInterface.Target {
      boolean called;

      @Override
      public void method(String foo) {
        called = true;
      }
    }
    Target target = new Target();
    component.inject(target);

    assertThat(target.called).isFalse();
  }

  @Test
  public void memberInjectionReturnInstance() {
    MemberInjectionReturnInstance component = backend.create(MemberInjectionReturnInstance.class);
    MemberInjectionReturnInstance.Target in = new MemberInjectionReturnInstance.Target();
    MemberInjectionReturnInstance.Target out = component.inject(in);
    assertThat(out.foo).isEqualTo("foo");
    assertThat(out).isSameInstanceAs(in);
  }

  @Test
  public void memberInjectionNoInjects() {
    MemberInjectionNoInjects component = backend.create(MemberInjectionNoInjects.class);
    MemberInjectionNoInjects.Target target = new MemberInjectionNoInjects.Target();
    component.inject(target);
    assertThat(target.one).isNull();
    assertThat(target.two).isNull();
    assertThat(target.three).isNull();
    assertThat(target.count).isEqualTo(0);
  }

  @Test
  public void memberInjectionFieldBeforeMethod() {
    MemberInjectionFieldBeforeMethod component =
        backend.create(MemberInjectionFieldBeforeMethod.class);
    MemberInjectionFieldBeforeMethod.Target target = new MemberInjectionFieldBeforeMethod.Target();
    component.inject(target);
    assertThat(target.fieldBeforeMethod).isTrue();
  }

  @Test
  public void memberInjectionFieldVisibility() {
    MemberInjectionFieldVisibility component = backend.create(MemberInjectionFieldVisibility.class);
    MemberInjectionFieldVisibility.Target target = new MemberInjectionFieldVisibility.Target();
    component.inject(target);
    assertThat(target.one).isEqualTo("one");
    assertThat(target.two).isEqualTo(2L);
    assertThat(target.three).isEqualTo(3);
  }

  @Test
  public void memberInjectionHierarchy() {
    MemberInjectionHierarchy component = backend.create(MemberInjectionHierarchy.class);
    MemberInjectionHierarchy.Subtype target = new MemberInjectionHierarchy.Subtype();
    component.inject(target);
    assertThat(target.baseOne).isEqualTo("foo");
    assertThat(target.baseCalled).isTrue();
    assertThat(target.subtypeOne).isEqualTo("foo");
    assertThat(target.subtypeCalled).isTrue();
  }

  @Test
  public void memberInjectionOrder() {
    MemberInjectionOrder component = backend.create(MemberInjectionOrder.class);
    MemberInjectionOrder.SubType target = new MemberInjectionOrder.SubType();
    component.inject(target);
    assertThat(target.calls)
        .containsExactly(
            // @Inject specification: Constructors are injected first
            "instantiation: baseField=null, subField=null",
            // followed by fields, and then methods.
            "baseMethod(foo): baseField=foo, subField=null",
            // Fields and methods in superclasses are injected before those in subclasses.
            "subMethod(foo): baseField=foo, subField=foo")
        .inOrder();
  }

  @Test
  public void memberInjectionMethodVisibility() {
    MemberInjectionMethodVisibility component =
        backend.create(MemberInjectionMethodVisibility.class);
    MemberInjectionMethodVisibility.Target target = new MemberInjectionMethodVisibility.Target();
    component.inject(target);
    assertThat(target.count).isEqualTo(3);
    assertThat(target.one).isEqualTo("one");
    assertThat(target.two).isEqualTo(2L);
    assertThat(target.three).isEqualTo(3);
  }

  @Test
  public void memberInjectionMethodMultipleParams() {
    MemberInjectionMethodMultipleParams component =
        backend.create(MemberInjectionMethodMultipleParams.class);
    MemberInjectionMethodMultipleParams.Target target =
        new MemberInjectionMethodMultipleParams.Target();
    component.inject(target);
    assertThat(target.one).isEqualTo("one");
    assertThat(target.two).isEqualTo(2L);
    assertThat(target.two2).isEqualTo(2L);
    assertThat(target.three).isEqualTo(3);
  }

  @Test
  public void memberInjectionMethodReturnTypes() {
    MemberInjectionMethodReturnTypes component =
        backend.create(MemberInjectionMethodReturnTypes.class);
    MemberInjectionMethodReturnTypes.Target target = new MemberInjectionMethodReturnTypes.Target();
    component.inject(target);
    assertThat(target.count).isEqualTo(3);
  }

  @Test
  public void memberInjectionQualified() {
    MemberInjectionQualified component = backend.create(MemberInjectionQualified.class);
    MemberInjectionQualified.Target target = new MemberInjectionQualified.Target();
    component.inject(target);
    assertThat(target.fromField).isEqualTo("foo");
    assertThat(target.fromMethod).isEqualTo("foo");
  }

  @Test
  public void reusableScoped() {
    // @Reusable has no formal definition of reuse semantics. As such, we simply validate that
    // common uses of it don't throw an exception. We do not ensure behavior compatibility with
    // dagger-compiler, although it's an option in the future.

    ReusableScoped component = backend.create(ReusableScoped.class);
    Object object = component.object();
    assertThat(object).isNotNull(); // Smoke test.

    ReusableScoped.Child subcomponent = component.child();
    Object childObject = subcomponent.object();
    assertThat(childObject).isNotNull(); // Smoke test.
    Runnable childRunnable = subcomponent.runnable();
    assertThat(childRunnable).isNotNull(); // Smoke test.
  }

  @Test
  public void reusableJustInTime() {
    ReusableScopedJustInTime component = backend.create(ReusableScopedJustInTime.class);
    assertThat(component.bar()).isNotNull(); // Smoke test.
  }

  @Test
  public void scoped() {
    Scoped component = backend.create(Scoped.class);
    Object value1 = component.value();
    Object value2 = component.value();
    assertThat(value1).isSameInstanceAs(value2);
  }

  @Test
  public void scopedWithMultipleAnnotations() {
    ScopedWithMultipleAnnotations component = backend.create(ScopedWithMultipleAnnotations.class);
    Object value1 = component.value();
    Object value2 = component.value();
    assertThat(value1).isSameInstanceAs(value2);
    Runnable runnable1 = component.runnable();
    Runnable runnable2 = component.runnable();
    assertThat(runnable1).isSameInstanceAs(runnable2);
  }

  @Test
  @IgnoreCodegen
  public void scopedWrong() {
    try {
      backend.create(ScopedWrong.class);
      fail();
    } catch (IllegalStateException e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "[Dagger/IncompatiblyScopedBindings] "
                  + "(sub)component scoped with [@javax.inject.Singleton()] may not reference bindings with different scopes:\n"
                  + "@com.example.ScopedWrong.Unrelated @Provides[com.example.ScopedWrong$Module1.value(…)]");
    }
  }

  @Test
  public void multibindingSet() {
    MultibindingSet component = backend.create(MultibindingSet.class);
    assertThat(component.values()).containsExactly("one", "two");
  }

  @Test
  public void multibindingSetEmpty() {
    MultibindingSetEmpty component = backend.create(MultibindingSetEmpty.class);
    assertThat(component.values()).isEmpty();
  }

  @Test
  public void multibindingSetElements() {
    MultibindingSetElements component = backend.create(MultibindingSetElements.class);
    assertThat(component.values()).containsExactly("one", "two");
  }

  @Test
  public void multibindingSetPrimitive() {
    MultibindingSetPrimitive component = backend.create(MultibindingSetPrimitive.class);
    assertThat(component.values()).containsExactly(1L, 2L);
  }

  @Test
  public void multibindingSetElementsPrimitive() {
    MultibindingSetElementsPrimitive component =
        backend.create(MultibindingSetElementsPrimitive.class);
    assertThat(component.values()).containsExactly(1L, 2L);
  }

  @Test
  public void multibindingProviderSet() {
    MultibindingProviderSet component = backend.create(MultibindingProviderSet.class);
    Provider<Set<String>> values = component.values();

    // Ensure the Provider is lazy in invoking and aggregating its backing @Provides methods.
    MultibindingProviderSet.Module1.oneCount.set(1);
    MultibindingProviderSet.Module1.twoCount.set(1);

    assertThat(values.get()).containsExactly("one1", "two1");
    assertThat(values.get()).containsExactly("one2", "two2");
  }

  @Test
  public void multibindingMap() {
    MultibindingMap component = backend.create(MultibindingMap.class);
    assertThat(component.values()).containsExactly("1", "one", "2", "two");
  }

  @Test
  public void multibindingMapEmpty() {
    MultibindingMapEmpty component = backend.create(MultibindingMapEmpty.class);
    assertThat(component.values()).isEmpty();
  }

  @Test
  public void multibindingMapClassKey() {
    MultibindingMapClassKey c = backend.create(MultibindingMapClassKey.class);
    assertThat(c.values())
        .containsExactly(Impl1.class, Impl1.INSTANCE, Impl2.class, Impl2.INSTANCE);
  }

  @Test
  public void multibindingMapPrimitiveKey() {
    MultibindingMapPrimitiveKey component = backend.create(MultibindingMapPrimitiveKey.class);
    assertThat(component.values()).containsExactly(1L, "one", 2L, "two");
  }

  @Test
  public void multibindingMapPrimitiveValue() {
    MultibindingMapPrimitiveValue component = backend.create(MultibindingMapPrimitiveValue.class);
    assertThat(component.values()).containsExactly("1", 1L, "2", 2L);
  }

  @Test
  public void multibindingMapNoUnwrap() {
    MultibindingMapNoUnwrap component = backend.create(MultibindingMapNoUnwrap.class);
    assertThat(component.values())
        .containsExactly(
            Annotations.tableKey(1, 1), "one",
            Annotations.tableKey(2, 3), "two");
  }

  @Test
  public void multibindingProviderMap() {
    MultibindingProviderMap component = backend.create(MultibindingProviderMap.class);
    Provider<Map<String, String>> values = component.values();

    // Ensure the Provider is lazy in invoking and aggregating its backing @Provides methods.
    MultibindingProviderMap.Module1.oneCount.set(1);
    MultibindingProviderMap.Module1.twoCount.set(1);

    assertThat(values.get()).containsExactly("1", "one1", "2", "two1");
    assertThat(values.get()).containsExactly("1", "one2", "2", "two2");
  }

  @Test
  public void multibindingMapProvider() {
    MultibindingMapProvider component = backend.create(MultibindingMapProvider.class);
    Map<String, Provider<String>> values = component.values();
    assertThat(values.keySet()).containsExactly("1", "2");

    // Ensure each Provider is lazy in invoking its backing @Provides method.
    MultibindingMapProvider.Module1.twoValue.set("two");
    assertThat(values.get("2").get()).isEqualTo("two");

    MultibindingMapProvider.Module1.oneValue.set("one");
    assertThat(values.get("1").get()).isEqualTo("one");
  }

  @Test
  @IgnoreCodegen
  public void multibindsAnnotationWrongType() {
    try {
      backend.create(MultibindsAnnotationWrongType.class);
      fail();
    } catch (IllegalStateException e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo("@Multibinds return type must be Set or Map: class java.lang.String");
    }
  }

  @Test
  public void moduleClass() {
    ModuleClass component = backend.create(ModuleClass.class);
    assertThat(component.string()).isEqualTo("foo");
  }

  @Test
  public void moduleClassAndInterfaceHierarchy() {
    ModuleClassAndInterfaceHierarchy component =
        backend.create(ModuleClassAndInterfaceHierarchy.class);
    assertThat(component.number()).isEqualTo(42);
  }

  @Test
  public void moduleClassAndInterfaceDuplicatesHierarchy() {
    ModuleClassAndInterfaceDuplicatesHierarchy component =
        backend.create(ModuleClassAndInterfaceDuplicatesHierarchy.class);
    assertThat(component.number()).isEqualTo(42);
  }

  @Test
  public void moduleClassHierarchy() {
    ModuleClassHierarchy component = backend.create(ModuleClassHierarchy.class);
    assertThat(component.number()).isEqualTo(42);
  }

  @Test
  public void moduleClassHierarchyStatics() {
    ModuleClassHierarchyStatics component = backend.create(ModuleClassHierarchyStatics.class);
    assertThat(component.string()).isEqualTo("foo");
  }

  @Test
  public void moduleInterface() {
    ModuleInterface component = backend.create(ModuleInterface.class);
    assertThat(component.number()).isEqualTo(42);
  }

  @Test
  public void moduleInterfaceHierarchy() {
    ModuleInterfaceHierarchy component = backend.create(ModuleInterfaceHierarchy.class);
    assertThat(component.number()).isEqualTo(42);
  }

  @Test
  public void moduleInterfaceWithDefaultMethodUnrelatedDoesNotAffectDagger() {
    ModuleInterfaceDefaultMethodUnrelated component =
        backend.create(ModuleInterfaceDefaultMethodUnrelated.class);
    assertThat(component.string()).isEqualTo("foo");
  }

  @Test
  @IgnoreCodegen
  public void moduleAbstractClassInstanceMethodNotAllowed() {
    try {
      backend.create(ModuleAbstractInstanceProvidesMethod.class);
      fail();
    } catch (IllegalStateException e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "com.example.ModuleAbstractInstanceProvidesMethod.Module1 is abstract and has instance"
                  + " @Provides methods. Consider making the methods static or including a non-abstract"
                  + " subclass of the module instead.");
    }
  }

  @Test
  @IgnoreCodegen
  public void moduleInterfaceWithDefaultMethodNotAllowed() {
    try {
      backend.create(ModuleInterfaceDefaultProvidesMethod.class);
      fail();
    } catch (IllegalStateException e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "com.example.ModuleInterfaceDefaultProvidesMethod.Module1 is abstract and has instance"
                  + " @Provides methods. Consider making the methods static or including a non-abstract"
                  + " subclass of the module instead.");
    }
  }

  @Test
  public void modulePrivateMethod() {
    ModulePrivateMethod component = backend.create(ModulePrivateMethod.class);
    assertThat(component.integer()).isEqualTo(42);
  }

  @Test
  public void moduleIncludes() {
    ModuleIncludes component = backend.create(ModuleIncludes.class);
    assertThat(component.string()).isEqualTo("5");
  }

  @Test
  public void moduleSubcomponentBindsBuilder() {
    ModuleSubcomponentBindsBuilder component = backend.create(ModuleSubcomponentBindsBuilder.class);
    assertThat(component.string()).isEqualTo("5");
  }

  @Test
  public void moduleSubcomponentBindsFactory() {
    ModuleSubcomponentBindsBuilder component = backend.create(ModuleSubcomponentBindsBuilder.class);
    assertThat(component.string()).isEqualTo("5");
  }

  @Test
  @IgnoreCodegen
  public void moduleSubcomponentBindsFactoryAndBuilder() {
    try {
      backend.create(ModuleSubcomponentBindsFactoryAndBuilder.class);
      fail();
    } catch (IllegalStateException e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "@Subcomponent has more than one @Subcomponent.Builder or @Subcomponent.Factory: ["
                  + "com.example.ModuleSubcomponentBindsFactoryAndBuilder.StringSubcomponent.Builder, "
                  + "com.example.ModuleSubcomponentBindsFactoryAndBuilder.StringSubcomponent.Factory]");
    }
  }

  @Test
  @IgnoreCodegen
  public void moduleSubcomponentNoFactoryOrBuilder() {
    try {
      backend.create(ModuleSubcomponentNoFactoryOrBuilder.class);
      fail();
    } catch (IllegalStateException e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "com.example.ModuleSubcomponentNoFactoryOrBuilder.StringSubcomponent "
                  + "doesn't have a @Subcomponent.Builder or @Subcomponent.Factory, "
                  + "which is required when used with @Module.subcomponents");
    }
  }

  @Test
  public void nestedComponent() {
    NestedComponent.MoreNesting.AndMore.TheComponent component =
        backend.create(NestedComponent.MoreNesting.AndMore.TheComponent.class);
    assertThat(component.string()).isEqualTo("foo");
  }

  @Test
  public void nestedComponentBuilder() {
    NestedComponent.MoreNesting.AndMore.TheComponent component =
        backend.builder(NestedComponent.MoreNesting.AndMore.TheComponent.Builder.class).build();
    assertThat(component.string()).isEqualTo("foo");
  }

  @Test
  public void primitiveAutoBoxing() {
    PrimitiveAutoBoxing component = backend.create(PrimitiveAutoBoxing.class);
    assertThat(component.getByte()).isEqualTo((byte) 8);
    assertThat(component.getShort()).isEqualTo((short) 16);
    assertThat(component.getInteger()).isEqualTo(32);
    assertThat(component.getLong()).isEqualTo(64L);
    assertThat(component.getFloat()).isEqualTo(-32.0f);
    assertThat(component.getDouble()).isEqualTo(-64.0);
    assertThat(component.getBoolean()).isEqualTo(true);
    assertThat(component.getCharacter()).isEqualTo('\u221E');
  }

  @Test
  public void primitiveAutoUnboxing() {
    PrimitiveAutoUnboxing component = backend.create(PrimitiveAutoUnboxing.class);
    assertThat(component.getByte()).isEqualTo((byte) 8);
    assertThat(component.getShort()).isEqualTo((short) 16);
    assertThat(component.getInt()).isEqualTo(32);
    assertThat(component.getLong()).isEqualTo(64L);
    assertThat(component.getFloat()).isEqualTo(-32.0f);
    assertThat(component.getDouble()).isEqualTo(-64.0);
    assertThat(component.getBoolean()).isEqualTo(true);
    assertThat(component.getChar()).isEqualTo('\u221E');
  }

  @Test
  @IgnoreCodegen
  public void providerCycle() {
    ProviderCycle component = backend.create(ProviderCycle.class);
    try {
      component.string();
      fail();
    } catch (IllegalStateException e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Dependency cycle for java.lang.String\n"
                  + " * Requested: java.lang.String\n"
                  + "     from @Provides[com.example.ProviderCycle$Module1.longToString(…)]\n"
                  + " * Requested: java.lang.Long\n"
                  + "     from @Provides[com.example.ProviderCycle$Module1.intToLong(…)]\n"
                  + " * Requested: java.lang.Integer\n"
                  + "     from @Provides[com.example.ProviderCycle$Module1.stringToInteger(…)]\n"
                  + " * Requested: java.lang.String\n"
                  + "     which forms a cycle.");
    }
  }

  @Test
  @IgnoreCodegen
  public void undeclaredModule() {
    UndeclaredModules.Builder builder = backend.builder(UndeclaredModules.Builder.class);
    try {
      builder.module(new UndeclaredModules.Module1());
      fail();
    } catch (IllegalStateException e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "@Component.Builder has setters for modules that aren't required: "
                  + "com.example.UndeclaredModules$Builder.module");
    }
  }

  @Test
  @IgnoreCodegen
  public void undeclaredDependencies() {
    UndeclaredDependencies.Builder builder = backend.builder(UndeclaredDependencies.Builder.class);
    try {
      builder.dep("hey");
      fail();
    } catch (IllegalStateException e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "@Component.Builder has setters for dependencies that aren't required: "
                  + "com.example.UndeclaredDependencies$Builder.dep");
    }
  }

  @Test
  @IgnoreCodegen
  public void membersInjectionWrongReturnType() {
    MembersInjectorWrongReturnType component = backend.create(MembersInjectorWrongReturnType.class);
    MembersInjectorWrongReturnType.Target instance = new MembersInjectorWrongReturnType.Target();
    try {
      component.inject(instance);
      fail();
    } catch (IllegalStateException e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Members injection methods may only return the injected type or void: "
                  + "com.example.MembersInjectorWrongReturnType.inject");
    }
  }

  @SuppressWarnings("OverridesJavaxInjectableMethod")
  @Test
  @IgnoreCodegen
  public void membersInjectionAbstractMethod() {
    MembersInjectionAbstractMethod component = backend.create(MembersInjectionAbstractMethod.class);
    MembersInjectionAbstractMethod.Target instance =
        new MembersInjectionAbstractMethod.Target() {
          @Override
          public void abstractMethod(String one) {}
        };
    try {
      component.inject(instance);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e)
          .hasMessageThat()
          .startsWith(
              "Methods with @Inject may not be abstract: "
                  + "com.example.MembersInjectionAbstractMethod.Target.abstractMethod");
    }
  }

  @SuppressWarnings("OverridesJavaxInjectableMethod")
  @Test
  @IgnoreCodegen
  public void membersInjectionInterfaceMethod() {
    MembersInjectionInterfaceMethod component =
        backend.create(MembersInjectionInterfaceMethod.class);
    MembersInjectionInterfaceMethod.Target instance =
        new MembersInjectionInterfaceMethod.Target() {
          @Override
          public void interfaceMethod(String one) {}
        };
    try {
      component.inject(instance);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e)
          .hasMessageThat()
          .startsWith(
              "Methods with @Inject may not be abstract: "
                  + "com.example.MembersInjectionInterfaceMethod.Target.interfaceMethod");
    }
  }

  @Test
  @IgnoreCodegen
  public void membersInjectionPrivateField() {
    MembersInjectionPrivateField component = backend.create(MembersInjectionPrivateField.class);
    MembersInjectionPrivateField.Target instance = new MembersInjectionPrivateField.Target();
    try {
      component.inject(instance);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e)
          .hasMessageThat()
          .startsWith(
              "Dagger does not support injection into private fields: "
                  + "com.example.MembersInjectionPrivateField.Target.privateField");
    }
  }

  @Test
  @IgnoreCodegen
  public void membersInjectionStaticField() {
    MembersInjectionStaticField component = backend.create(MembersInjectionStaticField.class);
    MembersInjectionStaticField.Target instance = new MembersInjectionStaticField.Target();
    try {
      component.inject(instance);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e)
          .hasMessageThat()
          .startsWith(
              "Dagger does not support injection into static fields: "
                  + "com.example.MembersInjectionStaticField.Target.staticField");
    }
  }

  @Test
  @IgnoreCodegen
  public void membersInjectionPrivateMethod() {
    MembersInjectionPrivateMethod component = backend.create(MembersInjectionPrivateMethod.class);
    MembersInjectionPrivateMethod.Target instance = new MembersInjectionPrivateMethod.Target();
    try {
      component.inject(instance);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e)
          .hasMessageThat()
          .startsWith(
              "Dagger does not support injection into private methods: "
                  + "com.example.MembersInjectionPrivateMethod.Target.privateMethod()");
    }
  }

  @Test
  @IgnoreCodegen
  public void membersInjectionStaticMethod() {
    MembersInjectionStaticMethod component = backend.create(MembersInjectionStaticMethod.class);
    MembersInjectionStaticMethod.Target instance = new MembersInjectionStaticMethod.Target();
    try {
      component.inject(instance);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e)
          .hasMessageThat()
          .startsWith(
              "Dagger does not support injection into static methods: "
                  + "com.example.MembersInjectionStaticMethod.Target.staticMethod()");
    }
  }

  @Test
  @IgnoreCodegen
  public void abstractClassCreateFails() {
    try {
      backend.create(AbstractComponent.class);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "com.example.AbstractComponent is not an interface. "
                  + "Only interfaces are supported.");
    }
  }

  @Test
  @IgnoreCodegen
  public void abstractClassBuilderFails() {
    AbstractComponent.Builder builder = backend.builder(AbstractComponent.Builder.class);
    try {
      builder.build();
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "com.example.AbstractComponent is not an interface. "
                  + "Only interfaces are supported.");
    }
  }

  @Test
  @IgnoreCodegen
  public void noComponentAnnotationCreateFails() {
    try {
      backend.create(NoAnnotation.class);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo("com.example.NoAnnotation lacks @Component annotation");
    }
  }

  @Test
  @IgnoreCodegen
  public void noComponentAnnotationBuilderFails() {
    try {
      backend.builder(NoAnnotation.Builder.class);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo("com.example.NoAnnotation lacks @Component annotation");
    }
  }

  @Test
  @IgnoreCodegen
  public void packagePrivateComponentFails() {
    try {
      backend.builder(PackagePrivateComponent.Builder.class);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Component interface com.example.PackagePrivateComponent "
                  + "must be public in order to be reflectively created");
    }
  }

  @Test
  @IgnoreCodegen
  public void abstractBuilderClassFails() {
    try {
      backend.builder(AbstractBuilderClass.Builder.class);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "com.example.AbstractBuilderClass.Builder is not an interface. "
                  + "Only interfaces are supported.");
    }
  }

  @Test
  @IgnoreCodegen
  public void noComponentBuilderAnnotationFails() {
    try {
      backend.builder(NoBuilderAnnotation.Builder.class);
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "com.example.NoBuilderAnnotation.Builder lacks " + "@Component.Builder annotation");
    }
  }

  @Test
  @IgnoreCodegen
  public void componentWithDependenciesCreateFails() {
    try {
      backend.create(ComponentWithDependencies.class);
      fail();
    } catch (IllegalStateException e) {
      assertThat(e).hasMessageThat().isEqualTo("java.lang.String must be set");
    }
  }

  @Test
  public void subcomponentProvision() {
    SubcomponentProvision.Nested nested = backend.create(SubcomponentProvision.class).nested();
    assertThat(nested.one()).isEqualTo("one");
    assertThat(nested.two()).isEqualTo(2L);
  }

  @Test
  public void subcomponentBuilderProvision() {
    SubcomponentBuilderProvision.Nested nested =
        backend
            .create(SubcomponentBuilderProvision.class)
            .nestedBuilder()
            .module2(new SubcomponentBuilderProvision.Nested.Module2(2L))
            .build();
    assertThat(nested.one()).isEqualTo("one");
    assertThat(nested.two()).isEqualTo(2L);
  }

  @Test
  public void subcomponentFactoryMethod() {
    SubcomponentFactoryMethod.Nested nested =
        backend
            .create(SubcomponentFactoryMethod.class)
            .createNested(new SubcomponentFactoryMethod.Nested.Module2(2L));
    assertThat(nested.one()).isEqualTo("one");
    assertThat(nested.two()).isEqualTo(2L);
  }

  @Test
  public void subcomponentFactoryProvision() {
    SubcomponentFactoryProvision.Nested nested =
        backend
            .create(SubcomponentFactoryProvision.class)
            .nestedFactory()
            .create(new SubcomponentFactoryProvision.Nested.Module2(2L));
    assertThat(nested.one()).isEqualTo("one");
    assertThat(nested.two()).isEqualTo(2L);
  }

  @Test
  public void componentBindingInstance() {
    ComponentBindingInstance instance = backend.create(ComponentBindingInstance.class);
    assertThat(instance).isSameInstanceAs(instance.self());
    assertThat(instance).isSameInstanceAs(instance.target().component);
  }

  @Test
  public void subcomponentBindingInstance() {
    SubcomponentBindingInstance component = backend.create(SubcomponentBindingInstance.class);
    SubcomponentBindingInstance.Sub subcomponent = component.sub();
    // TODO https://github.com/google/dagger/issues/1550
    // assertThat(subcomponent).isSameInstanceAs(subcomponent.self());
    SubcomponentBindingInstance.Target target = subcomponent.target();
    assertThat(component).isSameInstanceAs(target.component);
    assertThat(subcomponent).isSameInstanceAs(target.subcomponent);
  }

  @Test
  @ReflectBug("check not implemented")
  @IgnoreCodegen
  public void componentScopeCycle() {
    try {
      backend.create(ComponentScopeCycle.class);
      fail();
    } catch (IllegalStateException e) {
      assertThat(e).hasMessageThat().isEqualTo("TODO");
    }
  }

  @Test
  @ReflectBug("check not implemented")
  @IgnoreCodegen
  public void componentScopeCycleWithMultipleAnnotations() {
    try {
      backend.create(ComponentScopeCycleWithMultipleAnnotations.class);
      fail();
    } catch (IllegalStateException e) {
      assertThat(e).hasMessageThat().isEqualTo("TODO");
    }
  }

  @Test
  @ReflectBug("check not implemented")
  @IgnoreCodegen
  public void componentAndSubcomponentScopeCycle() {
    ComponentAndSubcomponentScopeCycle component =
        backend.create(ComponentAndSubcomponentScopeCycle.class);
    try {
      component.singleton();
      fail();
    } catch (IllegalStateException e) {
      assertThat(e).hasMessageThat().isEqualTo("TODO");
    }
  }

  @Test
  @ReflectBug("check not implemented")
  @IgnoreCodegen
  public void componentScopeDependsOnUnscoped() {
    try {
      backend.create(ComponentScopedDependsOnUnscoped.class);
      fail();
    } catch (IllegalStateException e) {
      assertThat(e).hasMessageThat().isEqualTo("TODO");
    }
  }

  @Test
  @IgnoreCodegen
  public void subcomponentScopeCycle() {
    SubcomponentScopeCycle.RequestComponent requestComponent =
        backend.create(SubcomponentScopeCycle.class).request();
    try {
      requestComponent.singleton();
      fail();
    } catch (IllegalStateException e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Detected scope annotation cycle:\n"
                  + "  * [@javax.inject.Singleton()]\n"
                  + "  * [@com.example.SubcomponentScopeCycle$Request()]\n"
                  + "  * [@javax.inject.Singleton()]");
    }
  }

  @Test
  @IgnoreCodegen
  public void subcomponentScopeDependsOnUnscoped() {
    SubcomponentScopedDependsOnUnscoped unscoped =
        backend.create(SubcomponentScopedDependsOnUnscoped.class);
    try {
      unscoped.scoped();
      fail();
    } catch (IllegalStateException e) {
      assertThat(e)
          .hasMessageThat()
          .isEqualTo(
              "Scope with annotations [@javax.inject.Singleton()] may not depend on unscoped");
    }
  }

  @Test
  public void nestedDependencyInterfaceTest() {
    String value = "my-value";
    String result =
        backend.factory(NestedDependencyInterfaceTest.Factory.class).create(() -> value).value();
    assertThat(result).isSameInstanceAs(value);
  }

  @Test
  public void multipleInterfacesRequestSameDependency() {
    String value = "my-value";
    String result =
        backend
            .factory(MultipleInterfacesRequestSameDependency.Factory.class)
            .create(() -> value)
            .value();
    assertThat(result).isSameInstanceAs(value);
  }
}
