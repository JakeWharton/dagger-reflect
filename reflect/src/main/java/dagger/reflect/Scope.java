package dagger.reflect;

import dagger.reflect.Binding.LinkedBinding;
import dagger.reflect.Binding.UnlinkedBinding;
import java.lang.annotation.Annotation;
import javax.inject.Provider;
import org.jetbrains.annotations.Nullable;

final class Scope {
  private final BindingMap bindings;
  private final JustInTimeLookup.Factory jitLookupFactory;
  /** The annotation denoting {@linkplain javax.inject.Scope scoped} bindings for this instance. */
  private final @Nullable Annotation annotation;
  private final @Nullable Scope parent;

  Scope(BindingMap bindings, JustInTimeLookup.Factory jitLookupFactory,
      @Nullable Annotation annotation, @Nullable Scope parent) {
    this.bindings = bindings;
    this.jitLookupFactory = jitLookupFactory;
    this.annotation = annotation;
    this.parent = parent;

    if (annotation != null && parent != null) {
      if (parent.annotation == null) {
        throw new IllegalStateException("Scope " + annotation + " may not depend on unscoped");
      }

      // Traverse ancestry chain looking for a duplicate annotation declaration.
      for (Scope ancestor = parent; ancestor != null; ancestor = ancestor.parent) {
        if (annotation.equals(ancestor.annotation)) {
          StringBuilder message = new StringBuilder("Detected scope annotation cycle:\n  * ")
              .append(annotation)
              .append('\n');
          // Re-traverse the ancestry from our parent up to the offending ancestor for the chain.
          for (Scope visit = parent; visit != ancestor; visit = visit.parent) {
            if (visit == null) throw new AssertionError(); // Checked by outer loop.
            message.append("  * ").append(visit.annotation).append('\n');
          }
          message.append("  * ").append(ancestor.annotation);
          throw new IllegalStateException(message.toString());
        }
      }
    }
  }

  /**
   * Look for a linked binding for {@code key} in this scope or anywhere in the parent scope chain.
   * If an unlinked binding is found for the key, perform linking before returning it.
   */
  private @Nullable LinkedBinding<?> findBinding(Key key) {
    Binding binding = bindings.get(key);
    if (binding != null) {
      return binding instanceof LinkedBinding<?>
          ? (LinkedBinding<?>) binding
          :  Linker.link(bindings, key, (UnlinkedBinding) binding);
    }

    return parent != null
        ? parent.findBinding(key)
        : null;
  }

  /**
   * Attempt to insert a binding for {@code key} as a result of a just-in-time lookup.
   * <p>
   * If {@code lookup} does not contain a scoping annotation or it contains a scoping annotation
   * which matches the one for this scope, the binding will be inserted, linked, and returned.
   * Otherwise, the parent scope (if any) will be recursively checked. If no matching scope
   * annotation is found by traversing the parents null will be returned.
   */
  private @Nullable LinkedBinding<?> putJitBinding(Key key, JustInTimeLookup lookup) {
    Binding jitBinding = lookup.binding;

    Annotation scope = lookup.scope;
    if (scope != null) {
      if (!scope.equals(annotation)) {
        return parent != null
            ? parent.putJitBinding(key, lookup)
            : null;
      }

      jitBinding = jitBinding.asScoped();
    }

    Binding binding = bindings.putIfAbsent(key, jitBinding);
    return binding instanceof LinkedBinding<?>
        ? (LinkedBinding<?>) binding
        : Linker.link(bindings, key, (UnlinkedBinding) binding);
  }

  Provider<?> getProvider(Key key) {
    LinkedBinding<?> binding = findBinding(key);
    if (binding != null) {
      return binding;
    }

    JustInTimeLookup jitLookup = jitLookupFactory.create(key);
    if (jitLookup != null) {
      LinkedBinding<?> jitBinding = putJitBinding(key, jitLookup);
      if (jitBinding == null) {
        throw new IllegalStateException(); // TODO nice error message with scope chain
      }
      return jitBinding;
    }

    throw new IllegalArgumentException("No provider available for " + key);
  }
}
