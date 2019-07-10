package dagger.reflect.lint;

import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.detector.api.Issue;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class DaggerReflectIssueRegistry extends IssueRegistry {
  @NotNull
  @Override
  public List<Issue> getIssues() {
    return Arrays.asList(
        WrongRetentionDetector.ISSUE_MISSING_RETENTION,
        WrongRetentionDetector.ISSUE_WRONG_RETENTION);
  }
}
