package dagger.reflect.lint.java;

import static com.android.tools.lint.checks.infrastructure.LintDetectorTest.java;
import static com.android.tools.lint.checks.infrastructure.TestLintTask.lint;

import com.android.tools.lint.checks.infrastructure.LintDetectorTest;
import dagger.reflect.lint.WrongRetentionDetector;
import org.junit.Test;

public final class WrongRetentionDetectorTest {

  @Test
  public void ignoresIrrelevantAnnotation() {
    lint()
        .files(
            java(
                "package foo;\n"
                    + "\n"
                    + "import static java.lang.annotation.ElementType.ANNOTATION_TYPE;\n"
                    + "\n"
                    + "import java.lang.annotation.Target;\n"
                    + "\n"
                    + "@Target(ANNOTATION_TYPE)\n"
                    + "public @interface OtherAnnotation {}"),
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
            java(
                "package foo;\n"
                    + "\n"
                    + "import foo.bar.Qualifier;\n"
                    + "\n"
                    + "@Qualifier\n"
                    + "@Retention(RUNTIME)\n"
                    + "public @interface MyQualifier {}"),
            QUALIFIER_STUB)
        .issues(WrongRetentionDetector.ISSUE_WRONG_RETENTION)
        .run()
        .expectClean();
  }

  @Test
  public void ignoresQualifierAnnotationWithRuntimeRetention() {
    lint()
        .files(
            java(
                "package foo;\n"
                    + "\n"
                    + "import static java.lang.annotation.RetentionPolicy.RUNTIME;\n"
                    + "\n"
                    + "import javax.inject.Qualifier;\n"
                    + "import java.lang.annotation.Retention;\n"
                    + "\n"
                    + "@Qualifier\n"
                    + "@Retention(RUNTIME)\n"
                    + "public @interface MyQualifier {}"),
            QUALIFIER_STUB)
        .issues(WrongRetentionDetector.ISSUE_WRONG_RETENTION)
        .run()
        .expectClean();
  }

  @Test
  public void reportsQualifierAnnotationWithWrongRetentionAsStaticImport() {
    lint()
        .files(
            java(
                "package foo;\n"
                    + "\n"
                    + "import static java.lang.annotation.RetentionPolicy.SOURCE;\n"
                    + "\n"
                    + "import javax.inject.Qualifier;\n"
                    + "import java.lang.annotation.Retention;\n"
                    + "\n"
                    + "@Qualifier\n"
                    + "@Retention(SOURCE)\n"
                    + "public @interface MyQualifier {}"),
            QUALIFIER_STUB)
        .issues(WrongRetentionDetector.ISSUE_WRONG_RETENTION)
        .run()
        .expect(
            "src/foo/MyQualifier.java:9: Error: Annotation used by Dagger Reflect must be annotated with @Retention(RUNTIME) but is @Retention(SOURCE). [WrongRetention]\n"
                + "@Retention(SOURCE)\n"
                + "~~~~~~~~~~~~~~~~~~\n"
                + "1 errors, 0 warnings")
        .expectFixDiffs(
            "Fix for src/foo/MyQualifier.java line 9: Replace with: `@Retention(RUNTIME)`:\n"
                + "@@ -9 +9\n"
                + "- @Retention(SOURCE)\n"
                + "+ @Retention(java.lang.annotation.RetentionPolicy.RUNTIME)");
  }

  @Test
  public void reportsQualifierAnnotationWithWrongRetentionAsNormalImport() {
    lint()
        .files(
            java(
                "package foo;\n"
                    + "\n"
                    + "import javax.inject.Qualifier;\n"
                    + "import java.lang.annotation.Retention;\n"
                    + "import java.lang.annotation.RetentionPolicy;\n"
                    + "\n"
                    + "@Qualifier\n"
                    + "@Retention(RetentionPolicy.SOURCE)\n"
                    + "public @interface MyQualifier {}"),
            QUALIFIER_STUB)
        .issues(WrongRetentionDetector.ISSUE_WRONG_RETENTION)
        .run()
        .expect(
            "src/foo/MyQualifier.java:8: Error: Annotation used by Dagger Reflect must be annotated with @Retention(RUNTIME) but is @Retention(SOURCE). [WrongRetention]\n"
                + "@Retention(RetentionPolicy.SOURCE)\n"
                + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "1 errors, 0 warnings")
        .expectFixDiffs(
            "Fix for src/foo/MyQualifier.java line 8: Replace with: `@Retention(RUNTIME)`:\n"
                + "@@ -8 +8\n"
                + "- @Retention(RetentionPolicy.SOURCE)\n"
                + "+ @Retention(java.lang.annotation.RetentionPolicy.RUNTIME)");
  }

  @Test
  public void reportsQualifierAnnotationWithWrongRetentionAsNormalImportWithAssignment() {
    lint()
        .files(
            java(
                "package foo;\n"
                    + "\n"
                    + "import javax.inject.Qualifier;\n"
                    + "import java.lang.annotation.Retention;\n"
                    + "import java.lang.annotation.RetentionPolicy;\n"
                    + "\n"
                    + "@Qualifier\n"
                    + "@Retention(value = RetentionPolicy.SOURCE)\n"
                    + "public @interface MyQualifier {}"),
            QUALIFIER_STUB)
        .issues(WrongRetentionDetector.ISSUE_WRONG_RETENTION)
        .run()
        .expect(
            "src/foo/MyQualifier.java:8: Error: Annotation used by Dagger Reflect must be annotated with @Retention(RUNTIME) but is @Retention(SOURCE). [WrongRetention]\n"
                + "@Retention(value = RetentionPolicy.SOURCE)\n"
                + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n"
                + "1 errors, 0 warnings")
        .expectFixDiffs(
            "Fix for src/foo/MyQualifier.java line 8: Replace with: `@Retention(RUNTIME)`:\n"
                + "@@ -8 +8\n"
                + "- @Retention(value = RetentionPolicy.SOURCE)\n"
                + "+ @Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)");
  }

  @Test
  public void reportsQualifierAnnotationWithoutRetention() {
    lint()
        .files(
            java(
                "package foo;\n"
                    + "\n"
                    + "import javax.inject.Qualifier;\n"
                    + "\n"
                    + "@Qualifier\n"
                    + "public @interface MyQualifier {}"),
            QUALIFIER_STUB)
        .issues(WrongRetentionDetector.ISSUE_WRONG_RETENTION)
        .run()
        .expect(
            "src/foo/MyQualifier.java:6: Error: Annotation used by Dagger Reflect must be annotated with @Retention(RUNTIME). [WrongRetention]\n"
                + "public @interface MyQualifier {}\n"
                + "                  ~~~~~~~~~~~\n"
                + "1 errors, 0 warnings")
        .expectFixDiffs(
            "Fix for src/foo/MyQualifier.java line 6: Add: `@Retention(RUNTIME)`:\n"
                + "@@ -5 +5\n"
                + "+ @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)");
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
            java(
                "package foo;\n"
                    + "\n"
                    + "import foo.bar.MapKey;\n"
                    + "\n"
                    + "@MapKey\n"
                    + "public @interface MyMapKey {}"),
            MAP_KEY_STUB)
        .issues(WrongRetentionDetector.ISSUE_WRONG_RETENTION)
        .run()
        .expectClean();
  }

  @Test
  public void ignoresMapKeyAnnotationWithRuntimeRetention() {
    lint()
        .files(
            java(
                "package foo;\n"
                    + "\n"
                    + "import static java.lang.annotation.RetentionPolicy.RUNTIME;\n"
                    + "\n"
                    + "import dagger.MapKey;\n"
                    + "import java.lang.annotation.Retention;\n"
                    + "\n"
                    + "@MapKey\n"
                    + "@Retention(RUNTIME)\n"
                    + "public @interface MyMapKey {}"),
            MAP_KEY_STUB)
        .issues(WrongRetentionDetector.ISSUE_WRONG_RETENTION)
        .run()
        .expectClean();
  }

  @Test
  public void reportsMapKeyAnnotationWithWrongRetention() {
    lint()
        .files(
            java(
                "package foo;\n"
                    + "\n"
                    + "import static java.lang.annotation.RetentionPolicy.SOURCE;\n"
                    + "\n"
                    + "import dagger.MapKey;\n"
                    + "import java.lang.annotation.Retention;\n"
                    + "\n"
                    + "@MapKey\n"
                    + "@Retention(SOURCE)\n"
                    + "public @interface MyMapKey {}"),
            MAP_KEY_STUB)
        .issues(WrongRetentionDetector.ISSUE_WRONG_RETENTION)
        .run()
        .expect(
            "src/foo/MyMapKey.java:9: Error: Annotation used by Dagger Reflect must be annotated with @Retention(RUNTIME) but is @Retention(SOURCE). [WrongRetention]\n"
                + "@Retention(SOURCE)\n"
                + "~~~~~~~~~~~~~~~~~~\n"
                + "1 errors, 0 warnings")
        .expectFixDiffs(
            "Fix for src/foo/MyMapKey.java line 9: Replace with: `@Retention(RUNTIME)`:\n"
                + "@@ -9 +9\n"
                + "- @Retention(SOURCE)\n"
                + "+ @Retention(java.lang.annotation.RetentionPolicy.RUNTIME)");
  }

  @Test
  public void reportsMapKeyAnnotationWithoutRetention() {
    lint()
        .files(
            java(
                "package foo;\n"
                    + "\n"
                    + "import dagger.MapKey;\n"
                    + "\n"
                    + "@MapKey\n"
                    + "public @interface MyMapKey {}"),
            MAP_KEY_STUB)
        .issues(WrongRetentionDetector.ISSUE_WRONG_RETENTION)
        .run()
        .expect(
            "src/foo/MyMapKey.java:6: Error: Annotation used by Dagger Reflect must be annotated with @Retention(RUNTIME). [WrongRetention]\n"
                + "public @interface MyMapKey {}\n"
                + "                  ~~~~~~~~\n"
                + "1 errors, 0 warnings")
        .expectFixDiffs(
            "Fix for src/foo/MyMapKey.java line 6: Add: `@Retention(RUNTIME)`:\n"
                + "@@ -5 +5\n"
                + "+ @java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)");
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
