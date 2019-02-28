package dagger.reflect;

import dagger.reflect.Binding.LinkedBinding;
import dagger.reflect.Binding.UnlinkedBinding;
import java.lang.annotation.Annotation;
import javax.inject.Provider;
import org.jetbrains.annotations.Nullable;

import static dagger.reflect.DaggerReflect.notImplemented;

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

  Provider<?> getProvider(Key key) {
    LinkedBinding<?> binding = findBinding(key);
    if (binding != null) {
      return binding;
    }

    JustInTimeLookup jitLookup = jitLookupFactory.create(key);
    if (jitLookup != null) {
      if (jitLookup.scope != null) {
        // TODO walk up hierarchy looking for a matching scope.
        throw notImplemented("Just-in-time scoped bindings");
      }

      Binding jitBinding = bindings.putIfAbsent(key, jitLookup.binding);
      return jitBinding instanceof LinkedBinding<?>
          ? (LinkedBinding<?>) jitBinding
          : Linker.link(bindings, key, (UnlinkedBinding) jitBinding);
    }

    throw new IllegalArgumentException("No provider available for " + key);
  }
}
