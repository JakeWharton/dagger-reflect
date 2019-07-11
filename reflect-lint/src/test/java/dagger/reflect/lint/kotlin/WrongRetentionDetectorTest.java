package dagger.reflect.lint.kotlin;

import static com.android.tools.lint.checks.infrastructure.LintDetectorTest.java;
import static com.android.tools.lint.checks.infrastructure.LintDetectorTest.kotlin;
import static com.android.tools.lint.checks.infrastructure.TestLintTask.lint;

import com.android.tools.lint.checks.infrastructure.LintDetectorTest;
import dagger.reflect.lint.WrongRetentionDetector;
import org.junit.Test;

public final class WrongRetentionDetectorTest {

  @Test
  public void ignoresIrrelevantAnnotation() {
    lint()
        .files(
            kotlin(
                "package foo\n"
                    + "\n"
                    + "@Target(AnnotationTarget.ANNOTATION_CLASS)\n"
                    + "internal annotation class OtherAnnotation"),
            QUALIFIER_STUB)
        .issues(WrongRetentionDetector.ISSUE_WRONG_RETENTION)
        .run()
        .expectClean();
  }

  // Qualifier Annotation

  @Test
  public void ignoresIrrelevantQualifierAnnotation() {
    lint()
        .files(
            java(
                "package foo.bar;\n"
                    + "\n"
                    + "import static java.lang.annotation.RetentionPolicy.RUNTIME;\n"
                    + "\n"
                    + "import java.lang.annotation.Retention;\n"
                    + "\n"
                    + "@Retention(RUNTIME)\n"
                    + "public @interface Qualifier {}"),
            kotlin(
                "package foo\n"
                    + "\n"
                    + "import foo.bar.Qualifier\n"
                    + "\n"
                    + "@Qualifier\n"
                    + "@Retention(AnnotationRetention.RUNTIME)\n"
                    + "internal annotation class MyQualifier"),
            QUALIFIER_STUB)
        .issues(WrongRetentionDetector.ISSUE_WRONG_RETENTION)
        .run()
        .expectClean();
  }

  @Test
  public void ignoresQualifierAnnotationWithRuntimeRetention() {
    lint()
        .files(
            kotlin(
                "package foo\n"
                    + "\n"
                    + "import javax.inject.Qualifier\n"
                    + "import kotlin.annotation.AnnotationRetention.RUNTIME\n"
                    + "\n"
                    + "@Qualifier\n"
                    + "@Retention(RUNTIME)\n"
                    + "internal annotation class MyQualifier"),
            QUALIFIER_STUB)
        .issues(WrongRetentionDetector.ISSUE_WRONG_RETENTION)
        .run()
        .expectClean();
  }

  @Test
  public void reportsQualifierAnnotationWithWrongRetentionAsStaticImport() {
    lint()
        .files(
            kotlin(
                "package foo\n"
                    + "\n"
                    + "import javax.inject.Qualifier\n"
                    + "import kotlin.annotation.AnnotationRetention.SOURCE\n"
                    + "\n"
                    + "@Qualifier\n"
                    + "@Retention(SOURCE)\n"
                    + "internal annotation class MyQualifier"),
            QUALIFIER_STUB)
        .issues(WrongRetentionDetector.ISSUE_WRONG_RETENTION)
        .run()
        .expect(
            "src/foo/MyQualifier.kt:7: Error: Annotation used by Dagger Reflect must be annotated with @Retention(RUNTIME) but is @Retention(SOURCE). [WrongRetention]\n"
                + "@Retention(SOURCE)\n"
                + "~~~~~~~~~~~~~~~~~~\n"
                + "1 errors, 0 warnings")
        .expectFixDiffs(
            "Fix for src/foo/MyQualifier.kt line 7: Replace with: `@Retention(RUNTIME)`:\n"
                + "@@ -7 +7\n"
                + "- @Retention(SOURCE)\n"
                + "+ @Retention(kotlin.annotation.AnnotationRetention.RUNTIME)");
  }

  @Test
  public void reportsQualifierAnnotationWithWrongRetentionAsNormalImport() {
    lint()
        .files(
            kotlin(
                "package foo\n"
                    + "\n"
                    + "import javax.inject.Qualifier\n"
                    + "\n"
                    + "@Qualifier\n"
                    + "@Retention(AnnotationRetention.SOURCE)\n"
                    + "internal annotation class MyQualifier"),
            QUALIFIER_STUB)
        .issues(WrongRetentionDetector.ISSUE_WRONG_RETENTION)
        .run()
        .expect(
            "src/foo/MyQualifier.kt:6: Error: Annotation used by Dagger Reflect must be annotated with @Retention(RUNTIME) but is @Retention(SOURCE). [WrongRetention]\n"
                + "@Retention(AnnotationRetention.SOURCE)\n"
                + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "1 errors, 0 warnings")
        .expectFixDiffs(
            "Fix for src/foo/MyQualifier.kt line 6: Replace with: `@Retention(RUNTIME)`:\n"
                + "@@ -6 +6\n"
                + "- @Retention(AnnotationRetention.SOURCE)\n"
                + "+ @Retention(kotlin.annotation.AnnotationRetention.RUNTIME)");
  }

  @Test
  public void reportsQualifierAnnotationWithWrongRetentionAsNormalImportWithAssignment() {
    lint()
        .files(
            kotlin(
                "package foo\n"
                    + "\n"
                    + "import javax.inject.Qualifier\n"
                    + "\n"
                    + "@Qualifier\n"
                    + "@Retention(value = AnnotationRetention.SOURCE)\n"
                    + "internal annotation class MyQualifier"),
            QUALIFIER_STUB)
        .issues(WrongRetentionDetector.ISSUE_WRONG_RETENTION)
        .run()
        .expect(
            "src/foo/MyQualifier.kt:6: Error: Annotation used by Dagger Reflect must be annotated with @Retention(RUNTIME) but is @Retention(SOURCE). [WrongRetention]\n"
                + "@Retention(value = AnnotationRetention.SOURCE)\n"
                + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "1 errors, 0 warnings")
        .expectFixDiffs(
            "Fix for src/foo/MyQualifier.kt line 6: Replace with: `@Retention(RUNTIME)`:\n"
                + "@@ -6 +6\n"
                + "- @Retention(value = AnnotationRetention.SOURCE)\n"
                + "+ @Retention(value = kotlin.annotation.AnnotationRetention.RUNTIME)");
  }

  @Test
  public void reportsQualifierAnnotationWithoutRetention() {
    lint()
        .files(
            kotlin(
                "package foo\n"
                    + "\n"
                    + "import javax.inject.Qualifier\n"
                    + "\n"
                    + "@Qualifier\n"
                    + "internal annotation class MyQualifier"),
            QUALIFIER_STUB)
        .issues(WrongRetentionDetector.ISSUE_WRONG_RETENTION)
        .run()
        .expect(
            "src/foo/MyQualifier.kt:6: Error: Annotation used by Dagger Reflect must be annotated with @Retention(RUNTIME). [WrongRetention]\n"
                + "internal annotation class MyQualifier\n"
                + "                          ~~~~~~~~~~~\n"
                + "1 errors, 0 warnings")
        .expectFixDiffs(
            "Fix for src/foo/MyQualifier.kt line 6: Add: `@Retention(RUNTIME)`:\n"
                + "@@ -5 +5\n"
                + "+ @kotlin.annotation.Retention(kotlin.annotation.AnnotationRetention.RUNTIME)");
  }

  // MapKey Annotation

  @Test
  public void ignoresIrrelevantMapKeyAnnotation() {
    lint()
        .files(
            java(
                "package foo.bar;\n"
                    + "\n"
                    + "import static java.lang.annotation.RetentionPolicy.RUNTIME;\n"
                    + "\n"
                    + "import java.lang.annotation.Retention;\n"
                    + "\n"
                    + "@Retention(RUNTIME)\n"
                    + "public @interface MapKey {}"),
            kotlin(
                "package foo\n"
                    + "\n"
                    + "import foo.bar.MapKey\n"
                    + "\n"
                    + "@MapKey\n"
                    + "internal annotation class MyMapKey"),
            MAP_KEY_STUB)
        .issues(WrongRetentionDetector.ISSUE_WRONG_RETENTION)
        .run()
        .expectClean();
  }

  @Test
  public void ignoresMapKeyAnnotationWithRuntimeRetention() {
    lint()
        .files(
            kotlin(
                "package foo\n"
                    + "\n"
                    + "import dagger.MapKey\n"
                    + "import kotlin.annotation.AnnotationRetention.RUNTIME\n"
                    + "\n"
                    + "@MapKey\n"
                    + "@Retention(RUNTIME)\n"
                    + "internal annotation class MyMapKey"),
            MAP_KEY_STUB)
        .issues(WrongRetentionDetector.ISSUE_WRONG_RETENTION)
        .run()
        .expectClean();
  }

  @Test
  public void reportsMapKeyAnnotationWithWrongRetention() {
    lint()
        .files(
            kotlin(
                "package foo\n"
                    + "\n"
                    + "import dagger.MapKey\n"
                    + "\n"
                    + "@MapKey\n"
                    + "@Retention(AnnotationRetention.SOURCE)\n"
                    + "internal annotation class MyMapKey"),
            MAP_KEY_STUB)
        .issues(WrongRetentionDetector.ISSUE_WRONG_RETENTION)
        .run()
        .expect(
            "src/foo/MyMapKey.kt:6: Error: Annotation used by Dagger Reflect must be annotated with @Retention(RUNTIME) but is @Retention(SOURCE). [WrongRetention]\n"
                + "@Retention(AnnotationRetention.SOURCE)\n"
                + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "1 errors, 0 warnings")
        .expectFixDiffs(
            "Fix for src/foo/MyMapKey.kt line 6: Replace with: `@Retention(RUNTIME)`:\n"
                + "@@ -6 +6\n"
                + "- @Retention(AnnotationRetention.SOURCE)\n"
                + "+ @Retention(kotlin.annotation.AnnotationRetention.RUNTIME)");
  }

  @Test
  public void reportsMapKeyAnnotationWithoutRetention() {
    lint()
        .files(
            kotlin(
                "package foo\n"
                    + "\n"
                    + "import dagger.MapKey\n"
                    + "\n"
                    + "@MapKey\n"
                    + "internal annotation class MyMapKey"),
            MAP_KEY_STUB)
        .issues(WrongRetentionDetector.ISSUE_WRONG_RETENTION)
        .run()
        .expect(
            "src/foo/MyMapKey.kt:6: Error: Annotation used by Dagger Reflect must be annotated with @Retention(RUNTIME). [WrongRetention]\n"
                + "internal annotation class MyMapKey\n"
                + "                          ~~~~~~~~\n"
                + "1 errors, 0 warnings")
        .expectFixDiffs(
            "Fix for src/foo/MyMapKey.kt line 6: Add: `@Retention(RUNTIME)`:\n"
                + "@@ -5 +5\n"
                + "+ @kotlin.annotation.Retention(kotlin.annotation.AnnotationRetention.RUNTIME)");
  }

  private static final LintDetectorTest.TestFile MAP_KEY_STUB =
      java(
          "package dagger;\n"
              + "\n"
              + "import static java.lang.annotation.ElementType.ANNOTATION_TYPE;\n"
              + "import static java.lang.annotation.RetentionPolicy.RUNTIME;\n"
              + "\n"
              + "import java.lang.annotation.Retention;\n"
              + "import java.lang.annotation.Target;\n"
              + "\n"
              + "@Target(ANNOTATION_TYPE)\n"
              + "@Retention(RUNTIME)\n"
              + "public @interface MapKey {\n"
              + "  boolean unwrapValue() default true;\n"
              + "}\n");

  private static final LintDetectorTest.TestFile QUALIFIER_STUB =
      java(
          "package javax.inject;\n"
              + "\n"
              + "import java.lang.annotation.Target;\n"
              + "import java.lang.annotation.Retention;\n"
              + "import java.lang.annotation.Documented;\n"
              + "import static java.lang.annotation.RetentionPolicy.RUNTIME;\n"
              + "import static java.lang.annotation.ElementType.ANNOTATION_TYPE;\n"
              + "\n"
              + "@Target(ANNOTATION_TYPE)\n"
              + "@Retention(RUNTIME)\n"
              + "public @interface Qualifier {}\n");
}
