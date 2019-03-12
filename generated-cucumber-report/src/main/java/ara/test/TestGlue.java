package ara.test;

import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.ContinueNextStepsFor;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.commons.io.IOUtils;

@SuppressWarnings("static-method")
public class TestGlue {

    private static String readStructuredEmbeddingsFile() throws IOException {
        return loadUtf8ResourceAsString("embedding/structured-embeddings.txt")
                // The file is formatted for easy editing: remove all formatting here
                // (note: indented by TABS to be able to remove them without altering the data)
                .replaceAll("[\t\r\n]", "");
    }

    private static InputStream openResourceStream(String fileName) {
        return TestGlue.class.getClassLoader().getResourceAsStream(fileName);
    }

    private static String loadUtf8ResourceAsString(String fileName) throws IOException {
        return IOUtils.toString(openResourceStream(fileName), StandardCharsets.UTF_8);
    }

    @Before("@fail-on-before")
    public void failOnBefore() {
        throw new RuntimeException("This scenario fails on before");
    }

    @After
    public void after(Scenario scenario) throws IOException {
        if (scenario.isFailed()) {
            // Take a fake screenshot
            byte[] screenshot = IOUtils.toByteArray(TestGlue.class.getClassLoader().getResourceAsStream("screenshots/four-pixels.png"));
            scenario.embed(screenshot, "image/png");

            // upload a fake video
            String videoUrl = "http://fake.video.server/" + scenario.getId() + ".mp4";
            scenario.embed(videoUrl.getBytes(StandardCharsets.UTF_8), "text/plain");
        }
    }

    @Given("^A step that works$")
    public void a_step_that_works() {
        // It works!
    }

    @ContinueNextStepsFor(RuntimeException.class)
    @Then("^A step number (\\d+) that fails with error \"([^\"]*)\"$")
    public void a_step_number_that_fails_with_error(@SuppressWarnings("unused") int number, String string) {
        throw new RuntimeException("Error message " + string);
    }

    @ContinueNextStepsFor(RuntimeException.class)
    @Then("^These values are true:$")
    public void these_values_are_true(@SuppressWarnings("unused") Map<String, String> values) {
        // We trust them: they are always true!
    }

    @After("@fail-on-after")
    public void failOnAfter() {
        throw new RuntimeException("This scenario fails on after");
    }

    @After("@add-structured-embeddings")
    public void addStructuredEmbeddings(Scenario scenario) throws IOException {
        // Copied from StructuredEmbeddingsJUnit in functional-tests-base project (the one producing the structured embeddings)
        scenario.embed(readStructuredEmbeddingsFile().getBytes(StandardCharsets.UTF_8), "text/html");
    }

    @Before("@another-before")
    public void anotherBeforeHook() {
        // Does nothing: just to check multiple-hooks are supported
    }

    @Given("^A doc string:$")
    public void aDocString(String docString) {
        // Does nothing: just to check doc strings indexation
    }

}
