package jenkins.plugins.office365connector.utils;

import hudson.util.FormValidation;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.validator.routines.UrlValidator;

/**
 * Collects method for forms.
 *
 * @author Joseph Petersen (casz@github)
 */
public class FormUtils {

    /**
     * Validates data is in expected format either URL or variable reference.
     *
     * @param value user input to validate
     * @return OK or Error on pending user input
     */
    public static FormValidation formValidateUrl(String value, String urlCredentialsId) {
        if (StringUtils.isBlank(value)) {
            if (!StringUtils.isBlank(urlCredentialsId)) {
                return FormValidation.ok();
            } else {
                return FormValidation.error("Either URL or URL Credentials must be non-empty");
            }
        }
        if (validateUrl(value)) {
            return FormValidation.ok();
        }
        return FormValidation.error("Valid URL or variable reference must be provided");
    }

    public static boolean validateUrl(String value) {
        if (value.startsWith("$") && value.length() > 1) {
            return true;
        }
        return UrlValidator.getInstance().isValid(value);
    }
}
