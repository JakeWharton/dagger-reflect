package dagger.reflect.lint;

import com.android.tools.lint.client.api.IssueRegistry;
import com.android.tools.lint.detector.api.Issue;
import java.util.Arrays;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public final class DaggerReflectIssueRegistry extends IssueRegistry {
  @NotNull
  @Override
  public List<Issue> getIssues() {
    return Arrays.asList(WrongRetentionDetector.ISSUE_WRONG_RETENTION);
  }
}
