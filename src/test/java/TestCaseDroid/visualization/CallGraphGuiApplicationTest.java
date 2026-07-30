package TestCaseDroid.visualization;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CallGraphGuiApplicationTest {
    @Test
    void addsVisualizationFlagToAnalysisArguments() {
        String[] result = CallGraphGuiApplication.withVisualization(
                new String[]{"--path", "target/classes"});

        assertTrue(Arrays.asList(result).contains("--visualize"));
    }

    @Test
    void doesNotDuplicateExistingVisualizationFlag() {
        String[] result = CallGraphGuiApplication.withVisualization(
                new String[]{"--path", "target/classes", "--visualize"});

        assertEquals(1, Arrays.stream(result)
                .filter("--visualize"::equals)
                .count());
    }

    @Test
    void leavesHelpArgumentsUnchanged() {
        String[] input = {"--help"};

        assertArrayEquals(input,
                CallGraphGuiApplication.withVisualization(input));
    }
}
