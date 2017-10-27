/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jenkins.plugins.office365connector;

import java.io.File;
import java.io.IOException;

import hudson.FilePath;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import jenkins.plugins.office365connector.model.Macro;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;

/**
 * Provides methods that help to decide if the notification should be sent or skipped.
 *
 * @author Damian Szczepanik (damianszczepanik@github)
 */
public class DecisionMaker {

    private final Run run;
    private final Result previousResult;

    private final TaskListener listener;

    public DecisionMaker(Run run, TaskListener listener) {
        this.run = run;
        this.listener = listener;

        Run previousBuild = run.getPreviousBuild();
        previousResult = previousBuild != null ? previousBuild.getResult() : Result.SUCCESS;
    }

    /**
     * Iterates over each macro for passed webhook and checks if at least one template matches to expected value.
     *
     * @param webhook webhook that should be examined
     * @return <code>true</code> if at least one macro has matched, <code>false</code> otherwise
     */
    public boolean isAtLeastOneRuleMatched(Webhook webhook) {
        if (webhook.getMacros().isEmpty()) {
            return true;
        } else {
            for (Macro macro : webhook.getMacros()) {
                String evaluated = evaluateMacro(macro.getTemplate());
                if (evaluated.equals(macro.getValue())) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Checks if notification should be passed by comparing current status and webhook configuration
     *
     * @param webhook webhook that will be verified
     * @return <code>true</code> if current status matches to webhook configuration
     */
    public boolean isStatusMatched(Webhook webhook) {
        Result result = run.getResult();

        return isNotifyAborted(result, webhook)
                || isNotifyFailure(result, webhook)
                || isNotifyRepeatedFailure(result, webhook)
                || isNotifyNotBuilt(result, webhook)
                || isNotifyBackToNormal(result, webhook)
                || isNotifySuccess(result, webhook)
                || isNotifyUnstable(result, webhook);
    }

    private boolean isNotifyAborted(Result result, Webhook webhook) {
        return result == Result.ABORTED
                && webhook.isNotifyAborted();
    }

    private boolean isNotifyFailure(Result result, Webhook webhook) {
        return result == Result.FAILURE && previousResult != Result.FAILURE
                && webhook.isNotifyFailure();
    }

    private boolean isNotifyRepeatedFailure(Result result, Webhook webhook) {
        return result == Result.FAILURE && previousResult == Result.FAILURE
                && webhook.isNotifyRepeatedFailure();
    }

    private boolean isNotifyNotBuilt(Result result, Webhook webhook) {
        return result == Result.NOT_BUILT
                && webhook.isNotifyNotBuilt();
    }

    private boolean isNotifyBackToNormal(Result result, Webhook webhook) {
        return result == Result.SUCCESS && (previousResult == Result.FAILURE || previousResult == Result.UNSTABLE)
                && webhook.isNotifyBackToNormal();
    }

    private boolean isNotifySuccess(Result result, Webhook webhook) {
        return result == Result.SUCCESS
                && webhook.isNotifySuccess();
    }

    private boolean isNotifyUnstable(Result result, Webhook webhook) {
        return result == Result.UNSTABLE
                && webhook.isNotifyUnstable();
    }

    private String evaluateMacro(String template) {
        try {
            File workspace = run.getRootDir();
            return TokenMacro.expandAll(run, new FilePath(workspace), listener, template);
        } catch (InterruptedException | IOException | MacroEvaluationException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
