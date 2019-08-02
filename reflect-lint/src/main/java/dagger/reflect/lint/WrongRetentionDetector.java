package dagger.reflect.lint;

import com.android.tools.lint.client.api.UElementHandler;
import com.android.tools.lint.detector.api.*;
import com.intellij.psi.PsiEnumConstant;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UAnnotation;
import org.jetbrains.uast.UClass;
import org.jetbrains.uast.UElement;
import org.jetbrains.uast.UExpression;

public final class WrongRetentionDetector extends Detector implements Detector.UastScanner {

  private static final String ANNOTATION_QUALIFIER = "javax.inject.Qualifier";
  private static final String ANNOTATION_MAP_KEY = "dagger.MapKey";
  private static final String ANNOTATION_RETENTION_JAVA = "java.lang.annotation.Retention";
  private static final String ANNOTATION_RETENTION_KOTLIN = "kotlin.annotation.Retention";

  private static final String CLASS_JAVA_RETENTION_POLICY = "java.lang.annotation.RetentionPolicy";
  private static final String CLASS_KOTLIN_RETENTION_POLICY =
      "kotlin.annotation.AnnotationRetention";

  private static final String FIX_RETENTION_TYPE_JAVA =
      "java.lang.annotation.RetentionPolicy.RUNTIME";
  private static final String FIX_RETENTION_TYPE_KOTLIN =
      "kotlin.annotation.AnnotationRetention.RUNTIME";
  private static final String FIX_ANNOTATION_RETENTION_JAVA =
      "@java.lang.annotation.Retention(" + FIX_RETENTION_TYPE_JAVA + ")\n";
  private static final String FIX_ANNOTATION_RETENTION_KOTLIN =
      "@kotlin.annotation.Retention(" + FIX_RETENTION_TYPE_KOTLIN + ")\n";

  @Override
  public List<Class<? extends UElement>> getApplicableUastTypes() {
    return Collections.singletonList(UClass.class);
  }

  @Override
  public UElementHandler createUastHandler(@NotNull JavaContext context) {
    return new UElementHandler() {
      @Override
      public void visitClass(@NotNull UClass node) {
        if (!node.isAnnotationType()) {
          return;
        }

        final UAnnotation qualifierAnnotation = node.findAnnotation(ANNOTATION_QUALIFIER);
        final UAnnotation mapKeyAnnotation = node.findAnnotation(ANNOTATION_MAP_KEY);
        if (qualifierAnnotation == null && mapKeyAnnotation == null) {
          return;
        }

        final boolean isKotlin = Lint.isKotlin(node);
        final UAnnotation retentionAnnotation =
            node.findAnnotation(isKotlin ? ANNOTATION_RETENTION_KOTLIN : ANNOTATION_RETENTION_JAVA);
        if (retentionAnnotation == null) {
          final UAnnotation reflectRelatedAnnotation =
              qualifierAnnotation != null ? qualifierAnnotation : mapKeyAnnotation;
          reportMissingRetention(context, isKotlin, node, reflectRelatedAnnotation);
        } else {
          final String retentionPolicy = getRetentionPolicy(context, isKotlin, retentionAnnotation);
          if (!"RUNTIME".equals(retentionPolicy)) {
            reportWrongRetentionType(context, isKotlin, retentionAnnotation, retentionPolicy);
          }
        }
      }
    };
  }

  private static void reportMissingRetention(
      @NotNull JavaContext context,
      boolean isKotlin,
      @NotNull UClass node,
      @NotNull UAnnotation reflectRelatedAnnotation) {
    context.report(
        ISSUE_WRONG_RETENTION,
        node,
        context.getNameLocation(node),
        "Annotation used by Dagger Reflect must be annotated with `@Retention(RUNTIME)`.",
        LintFix.create()
            .replace()
            .name("Add: `@Retention(RUNTIME)`")
            .range(context.getLocation(reflectRelatedAnnotation))
            .beginning()
            .with(isKotlin ? FIX_ANNOTATION_RETENTION_KOTLIN : FIX_ANNOTATION_RETENTION_JAVA)
            .reformat(true)
            .shortenNames()
            .build());
  }

  private static void reportWrongRetentionType(
      @NotNull JavaContext context,
      boolean isKotlin,
      @NotNull UAnnotation retentionAnnotation,
      @NotNull String actualRetention) {
    final UExpression annotationValue = UastLintUtils.getAnnotationValue(retentionAnnotation);
    context.report(
        ISSUE_WRONG_RETENTION,
        retentionAnnotation,
        context.getLocation(retentionAnnotation),
        String.format(
            "Annotation used by Dagger Reflect must be annotated with `@Retention(RUNTIME)` but is `@Retention(%s)`.",
            actualRetention),
        LintFix.create()
            .name("Replace with: `@Retention(RUNTIME)`")
            .replace()
            .range(context.getLocation(annotationValue))
            .with(isKotlin ? FIX_RETENTION_TYPE_KOTLIN : FIX_RETENTION_TYPE_JAVA)
            .reformat(true)
            .shortenNames()
            .build());
  }

  @NotNull
  private static String getRetentionPolicy(
      @NotNull JavaContext context, boolean isKotlin, @NotNull UAnnotation retentationAnnotation) {
    final UExpression annotationValue = UastLintUtils.getAnnotationValue(retentationAnnotation);
    final String retentionPolicyQualifiedName =
        isKotlin
            ? getQualifiedNameForValueKotlin(context, annotationValue)
            : getQualifiedNameForValueJava(context, annotationValue);
    final String retentionPolicy = getRetentionPolicyForQualifiedName(retentionPolicyQualifiedName);
    if (retentionPolicy != null) {
      return retentionPolicy;
    }
    throw new IllegalStateException("RetentionPolicy must not be null if @Retention is present");
  }

  @NotNull
  private static String getQualifiedNameForValueKotlin(
      @NotNull JavaContext context, @Nullable UExpression annotationValue) {
    final Object evaluatedAnnotationValue = ConstantEvaluator.evaluate(context, annotationValue);
    if (evaluatedAnnotationValue instanceof kotlin.Pair) {
      final kotlin.Pair<?, ?> value = (kotlin.Pair<?, ?>) evaluatedAnnotationValue;
      final String qualifiedName = (value.getFirst() + "." + value.getSecond());
      return qualifiedName.replace("/", ".");
    }
    throw new IllegalStateException("RetentionPolicy must not be null if @Retention is present");
  }

  @NotNull
  private static String getQualifiedNameForValueJava(
      @NotNull JavaContext context, @Nullable UExpression annotationValue) {
    final Object evaluatedAnnotationValue = ConstantEvaluator.evaluate(context, annotationValue);
    if (evaluatedAnnotationValue instanceof PsiEnumConstant) {
      return UastLintUtils.getQualifiedName((PsiEnumConstant) evaluatedAnnotationValue);
    }
    throw new IllegalStateException("RetentionPolicy must not be null if @Retention is present");
  }

  @Nullable
  private static String getRetentionPolicyForQualifiedName(@NotNull String retentionPolicy) {
    // Values are same for Kotlin and Java
    for (RetentionPolicy policy : RetentionPolicy.values()) {
      final String javaQualifiedName = CLASS_JAVA_RETENTION_POLICY + "." + policy.name();
      final String kotlinQualifiedName = CLASS_KOTLIN_RETENTION_POLICY + "." + policy.name();
      if (javaQualifiedName.equals(retentionPolicy)
          || kotlinQualifiedName.equals(retentionPolicy)) {
        return policy.name();
      }
    }
    return null;
  }

  public static final Issue ISSUE_WRONG_RETENTION =
      Issue.create(
          "WrongRetention",
          "Dagger annotations need to have Runtime Retention",
          "To make annotation accessible during runtime for Dagger Reflect, "
              + "the need to have the Retention annotation with the runtime RetentionPolicy.",
          Category.CORRECTNESS,
          10,
          Severity.ERROR,
          new Implementation(WrongRetentionDetector.class, Scope.JAVA_FILE_SCOPE));
}
