import com.intuit.karate.Results;
import com.intuit.karate.Runner;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestParallel {
    @Test
    void sanityCheck() {
        Results results = Runner.path("classpath:features").tags("@sanity-check,@medium-check").parallel(1);
        assertEquals(0, results.getFailCount(), results.getErrorMessages());
    }
}
