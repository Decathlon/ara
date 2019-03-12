package com.decathlon.ara.report.util;

import com.decathlon.ara.report.support.ResultsWithMatch;
import com.decathlon.ara.domain.Error;
import com.decathlon.ara.report.bean.Status;
import com.decathlon.ara.report.bean.Step;
import java.util.ArrayList;
import java.util.List;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

/**
 * A set of static functions with no dependency nor side-effect (no download, upload, database access...) that take a parsed Cucumber's report.json and extract errors in it.
 */
@UtilityClass
public class ErrorExtractorUtil {

    public static List<Error> extractErrors(List<String> stepDefinitions, ResultsWithMatch[] stepsOrHooks, String hookName) {
        final List<Error> errors = new ArrayList<>();
        if (stepsOrHooks != null) {
            for (int i = 0; i < stepsOrHooks.length; i++) {
                final ResultsWithMatch stepOrHook = stepsOrHooks[i];
                Error error = extractError(stepDefinitions, stepOrHook, hookName, i);
                if (error != null) {
                    errors.add(error);
                }
            }
        }
        return errors;
    }

    private static Error extractError(List<String> stepDefinitions, final ResultsWithMatch stepOrHook, String hookName, int hookIndex) {
        final String errorMessage = extractErrorMessage(stepOrHook);
        if (StringUtils.isEmpty(errorMessage)) {
            return null;
        }

        Error error = new Error();
        error.setStep(extractStep(stepOrHook, hookName));
        error.setStepDefinition(StepDefinitionUtil.extractStepDefinition(stepOrHook, hookName, stepDefinitions));
        error.setStepLine(extractStepLine(stepOrHook, hookName, hookIndex));
        error.setException(errorMessage);
        return error;
    }

    private static String extractErrorMessage(final ResultsWithMatch stepOrHook) {
        if (stepOrHook.getResult() == null) {
            return null;
        }
        String errorMessage = stepOrHook.getResult().getErrorMessage();
        if (StringUtils.isEmpty(errorMessage) && stepOrHook.getResult().getStatus() == Status.UNDEFINED && stepOrHook instanceof Step) {
            errorMessage = "Undefined step: " + ((Step) stepOrHook).getName();
        }
        if (StringUtils.isEmpty(errorMessage)) {
            return null;
        }
        // No matter how and where the exception was created, we always store and compare Unix line-endings
        return errorMessage.replace("\r\n", "\n");
    }

    private static String extractStep(ResultsWithMatch stepOrHook, String hookName) {
        final String step;
        if (StringUtils.isNotEmpty(hookName)) {
            step = hookName;
        } else if (stepOrHook instanceof Step) {
            step = ((Step) stepOrHook).getName();
        } else {
            step = stepOrHook.getMatch().getLocation();
        }
        return step;
    }

    private static int extractStepLine(ResultsWithMatch stepOrHook, String hookName, int hookIndex) {
        int stepLine;
        if (stepOrHook instanceof Step) {
            stepLine = ((Step) stepOrHook).getLine().intValue();
        } else {
            stepLine = CucumberReportUtil.virtualHookLine(hookName, hookIndex);
        }
        return stepLine;
    }

}
