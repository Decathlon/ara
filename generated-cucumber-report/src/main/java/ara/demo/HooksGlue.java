package ara.demo;

import com.decathlon.ara.lib.embed.producer.EmbeddingPriority;
import com.decathlon.ara.lib.embed.producer.StructuredEmbeddingsBuilder;
import com.decathlon.ara.lib.embed.producer.type.ImageEmbedding;
import com.decathlon.ara.lib.embed.producer.type.LinkEmbedding;
import com.decathlon.ara.lib.embed.producer.type.TextEmbedding;
import com.decathlon.ara.lib.embed.producer.type.VideoEmbedding;
import cucumber.api.Scenario;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import java.nio.charset.StandardCharsets;
import java.util.Random;

public class HooksGlue {

    /**
     * The amount of errors to generate in reports.<br>
     * In interval [0..2]:
     * <ul>
     *     <li>0: everything will succeed</li>
     *     <li>1: all steps that can fail, except one, will fail</li>
     *     <li>2: all steps that can fail will fail</li>
     * </ul>
     */
    public static int failingLevel;

    /**
     * The name of the failing scenario. Matches screenshot, video and logs files (without extensions)
     * exposed by the web server to fake result data in ARA.
     */
    static String failedScenario = null;

    private static Random random = new Random();

    private StructuredEmbeddingsBuilder embeddings;

    /**
     * The step durations will be displayed at the right of scenarios in ARA.<br>
     * Make sure to display something credible, while not being too long to generate.
     */
    static void simulateExecution() {
        try {
            Thread.sleep(5 + random.nextInt(10));
        } catch (@SuppressWarnings("unused") final InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Before
    public void before() {
        simulateExecution();
        embeddings = new StructuredEmbeddingsBuilder();
        // Not using embeddings.addStartDateTime(): need a predefined placeholder to replace it later by current date
        embeddings.add(new TextEmbedding(
                "startDateTime",
                "Scenario start date & time",
                "2019.04.09-09h03m27.164",
                EmbeddingPriority.TECHNICAL_DEBUG_SMALL));
    }

    @After
    public void after(Scenario scenario) {
        if (scenario.isFailed()) {
            // Simulate all embeddings to showcase them
            String araUrl = "/demo-files/"; // When viewed in ARA, will point to the ARA server that exposes these files

            String screenshotUrl = araUrl + "screenshots/" + failedScenario + ".png";
            String videoUrl = araUrl + "videos/" + failedScenario + ".mp4";
            String logsUrl = araUrl + "logs/" + failedScenario + ".txt";
            String httpRequestsUrl = araUrl + "http-requests.txt";
            String javaScriptErrorsUrl = araUrl + "javascript-errors.txt";
            String diffReportUrl = araUrl + "diff-report.html";
            String apiServer = (random.nextBoolean() ? "API01" : "API02");
            String seleniumNode = "firefox0" + (1 + random.nextInt(9)) + ".nodes.selenium.project.company.com";

            embeddings.add(new ImageEmbedding("screenshotUrl", "Screenshot", screenshotUrl, EmbeddingPriority.FUNCTIONAL_DEBUG_MEDIUM));
            embeddings.add(new VideoEmbedding("videoUrl", "Video", videoUrl, EmbeddingPriority.FUNCTIONAL_DEBUG_LARGE));
            embeddings.add(new LinkEmbedding("logsUrl", "Logs", logsUrl, EmbeddingPriority.TECHNICAL_DEBUG_MEDIUM));
            embeddings.add(new LinkEmbedding("httpRequestsUrl", "HTTP requests", httpRequestsUrl, EmbeddingPriority.TECHNICAL_DEBUG_LARGE));
            embeddings.add(new LinkEmbedding("javaScriptErrorsUrl", "JavaScript errors", javaScriptErrorsUrl, EmbeddingPriority.TECHNICAL_DEBUG_MEDIUM));
            embeddings.add(new LinkEmbedding("diffReportUrl", "Diff report", diffReportUrl, EmbeddingPriority.OUTPUT_LARGE));
            embeddings.add(new TextEmbedding("apiServer", "API server used by this HTTP session", apiServer, EmbeddingPriority.TECHNICAL_DEBUG_SMALL));
            embeddings.add(new TextEmbedding("seleniumNode", "Selenium node used by this scenario", seleniumNode, EmbeddingPriority.TECHNICAL_DEBUG_SMALL));

            // Done
            scenario.embed(embeddings.build().getBytes(StandardCharsets.UTF_8), "text/html");
        }
    }

}
