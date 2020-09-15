package dagger.reflect.lint;

import com.android.tools.lint.client.api.UElementHandler;
import com.android.tools.lint.detector.api.*;
import com.intellij.psi.PsiEnumConstant;
import java.lang.annotation.RetentionPolicy;
import java.util.Collections;
import java.util.List;
import kotlin.annotation.AnnotationRetention;
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
        if (retentionAnnotation != null) {
          final String retentionPolicy = getRetentionPolicy(context, isKotlin, retentionAnnotation);
          if (!"RUNTIME".equals(retentionPolicy)) {
            reportWrongRetentionType(context, isKotlin, retentionAnnotation, retentionPolicy);
          }
        } else if (!isKotlin) {
          final UAnnotation reflectRelatedAnnotation =
              qualifierAnnotation != null ? qualifierAnnotation : mapKeyAnnotation;
          reportMissingRetention(context, node, reflectRelatedAnnotation);
        }
      }
    };
  }

  private static void reportMissingRetention(
      @NotNull JavaContext context,
      @NotNull UClass node,
      @NotNull UAnnotation reflectRelatedAnnotation) {
    context.report(
        ISSUE_WRONG_RETENTION,
        node,
        context.getNameLocation(node),
        "Java annotations used by Dagger Reflect must be annotated with `@Retention(RUNTIME)`.",
        LintFix.create()
            .replace()
            .name("Add: `@Retention(RUNTIME)`")
            .range(context.getLocation(reflectRelatedAnnotation))
            .beginning()
            .with(FIX_ANNOTATION_RETENTION_JAVA)
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
            "Annotations used by Dagger Reflect must have RUNTIME retention. Found %s.",
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
      @NotNull JavaContext context, boolean isKotlin, @NotNull UAnnotation retentionAnnotation) {
    final UExpression annotationValue = UastLintUtils.getAnnotationValue(retentionAnnotation);
    if (isKotlin) {
      return getRetentionPolicyKotlin(context, annotationValue);
    } else {
      return getRetentionPolicyJava(context, annotationValue);
    }
  }

  @NotNull
  private static String getRetentionPolicyKotlin(
      @NotNull JavaContext context, @Nullable UExpression annotationValue) {
    final Object evaluatedAnnotationValue = ConstantEvaluator.evaluate(context, annotationValue);
    if (evaluatedAnnotationValue instanceof kotlin.Pair) {
      final kotlin.Pair<?, ?> value = (kotlin.Pair<?, ?>) evaluatedAnnotationValue;
      final String qualifiedName = (value.getFirst() + "." + value.getSecond()).replace("/", ".");
      for (AnnotationRetention retention : AnnotationRetention.values()) {
        if (qualifiedName.equals(CLASS_KOTLIN_RETENTION_POLICY + "." + retention.name())) {
          return retention.name();
        }
      }
    }
    throw new IllegalStateException("AnnotationRetention not found");
  }

  @NotNull
  private static String getRetentionPolicyJava(
      @NotNull JavaContext context, @Nullable UExpression annotationValue) {
    final Object evaluatedAnnotationValue = ConstantEvaluator.evaluate(context, annotationValue);
    if (evaluatedAnnotationValue instanceof PsiEnumConstant) {
      final String qualifiedName = UastLintUtils.getQualifiedName(
          (PsiEnumConstant) evaluatedAnnotationValue);
      for (RetentionPolicy policy : RetentionPolicy.values()) {
        if (qualifiedName.equals(CLASS_JAVA_RETENTION_POLICY + "." + policy.name())) {
          return policy.name();
        }
      }
    }
    throw new IllegalStateException("RetentionPolicy must not be null if @Retention is present");
  }

  public static final Issue ISSUE_WRONG_RETENTION =
      Issue.create(
          "WrongRetention",
          "Annotations used by Dagger Reflect must have RUNTIME retention",
          "Annotations with SOURCE or CLASS/BINARY retention are not visible to reflection",
          Category.CORRECTNESS,
          10,
          Severity.ERROR,
          new Implementation(WrongRetentionDetector.class, Scope.JAVA_FILE_SCOPE));
}
