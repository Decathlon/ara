package com.decathlon.ara.report.util;

import com.decathlon.ara.report.bean.Argument;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

@SuppressWarnings("static-method")
public class StepDefinitionUtilTest {

    @Test
    public void testSimulateMatchingStepDefinition() {
        String stepName = "A step number 1 that fails with error \"string parameter 1\"";
        Argument[] arguments = { new Argument("1", 14), new Argument("string parameter 1", 39) };
        String simulatedStepDefinition = StepDefinitionUtil.simulateMatchingStepDefinition(stepName, arguments);
        assertThat(simulatedStepDefinition).isEqualTo("^A step number (\\d+) that fails with error \"([^\"]*)\"$");
    }

    @Test
    public void testArgumentAtStart() {
        String stepName = "A1";
        Argument[] arguments = { new Argument("1", 1) };
        String simulatedStepDefinition = StepDefinitionUtil.simulateMatchingStepDefinition(stepName, arguments);
        assertThat(simulatedStepDefinition).isEqualTo("^A(\\d+)$");
    }

    @Test
    public void testArgumentAtEnd() {
        String stepName = "1A";
        Argument[] arguments = { new Argument("1", 0) };
        String simulatedStepDefinition = StepDefinitionUtil.simulateMatchingStepDefinition(stepName, arguments);
        assertThat(simulatedStepDefinition).isEqualTo("^(\\d+)A$");
    }

    @Test
    public void testIntegerArgumentAsString() {
        String stepName = "A\"1\"B";
        Argument[] arguments = { new Argument("1", 2) };
        String simulatedStepDefinition = StepDefinitionUtil.simulateMatchingStepDefinition(stepName, arguments);
        assertThat(simulatedStepDefinition).isEqualTo("^A\"([^\"]*)\"B$");
    }

    @Test
    public void testNoArgument() {
        String simulatedStepDefinition = StepDefinitionUtil.simulateMatchingStepDefinition("abc", new Argument[0]);
        assertThat(simulatedStepDefinition).isEqualTo("^abc$");
    }

    @Test
    public void testSpecialCharacters() {
        String original = "Test <([{\\^-=$!|]})?*+.>";
        String replaced = "Test " +
                "\\<" +
                "\\(" +
                "\\[" +
                "\\{" +
                "\\\\" +
                "\\^" +
                "\\-" +
                "\\=" +
                "\\$" +
                "\\!" +
                "\\|" +
                "\\]" +
                "\\}" +
                "\\)" +
                "\\?" +
                "\\*" +
                "\\+" +
                "\\." +
                "\\>";
        assertThat(StepDefinitionUtil.simulateMatchingStepDefinition(original, new Argument[0])).isEqualTo("^" + replaced + "$");
    }

}
