package dagger.reflect;

import dagger.reflect.Binding.LinkedBinding;
import dagger.reflect.Binding.UnlinkedBinding;
import java.util.LinkedHashMap;
import java.util.Map;
import org.jetbrains.annotations.Nullable;

final class Linker {
  private final Scope scope;
  private final Map<Key, Binding> chain = new LinkedHashMap<>();

  Linker(Scope scope) {
    this.scope = scope;
  }

  LinkedBinding<?> get(Key key) {
    LinkedBinding<?> binding = find(key);
    if (binding != null) {
      return binding;
    }
    throw failure(key, "Missing binding", "was not found");
  }

  @Nullable
  LinkedBinding<?> find(Key key) {
    return scope.findBinding(key, this);
  }

  LinkedBinding<?> link(Key key, UnlinkedBinding unlinkedBinding) {
    if (chain.containsKey(key)) {
      throw failure(key, "Dependency cycle", "forms a cycle");
    }
    chain.put(key, unlinkedBinding);
    LinkedBinding<?> linkedBinding = unlinkedBinding.link(this, scope);
    chain.remove(key);

    return linkedBinding;
  }

  private RuntimeException failure(Key key, String reason, String cause) {
    StringBuilder builder = new StringBuilder(reason).append(" for ").append(key).append('\n');
    appendChain(builder);
    builder.append(" * Requested: ").append(key).append("\n     which ").append(cause).append('.');
    throw new IllegalStateException(builder.toString());
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Linker with ").append(scope).append("\n");
    appendChain(builder);
    return builder.toString();
  }

  private void appendChain(StringBuilder builder) {
    for (Map.Entry<Key, Binding> entry : chain.entrySet()) {
      builder
          .append(" * Requested: ")
          .append(entry.getKey())
          .append("\n     from ")
          .append(entry.getValue())
          .append('\n');
    }
  }
}
