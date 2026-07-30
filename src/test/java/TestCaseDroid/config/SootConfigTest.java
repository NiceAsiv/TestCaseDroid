package TestCaseDroid.config;

import org.junit.jupiter.api.Test;
import soot.Scene;

import java.nio.file.Paths;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SootConfigTest {
    private static final String CLASS = "TestCaseDroid.test.callgraph.CallGraphExamples";
    private static final String CLASSES = Paths.get("target", "classes").toAbsolutePath().toString();
    private static final String ENTRY = "<" + CLASS + ": void diamondEntry()>";

    @Test
    void storesCallGraphAlgorithm() {
        SootConfig config = new SootConfig();
        config.setCallGraphAlgorithm("Spark");
        assertEquals("Spark", config.getCallGraphAlgorithm());
    }

    @Test
    void resetsSceneLoadsApplicationAndUsesExplicitEntryPoint() {
        SootConfig config = new SootConfig();
        config.setupSoot(CLASS, true, CLASSES, Collections.singletonList(ENTRY));

        assertNotNull(Scene.v().getSootClassPath());
        assertTrue(Scene.v().getSootClass(CLASS).isApplicationClass());
        assertEquals(Collections.singletonList(Scene.v().getMethod(ENTRY)),
                Scene.v().getEntryPoints());
        assertTrue(Scene.v().getCallGraph().size() > 0);
    }

    @Test
    void rejectsMissingClassPathAndUnknownAlgorithm() {
        SootConfig config = new SootConfig();
        assertThrows(IllegalArgumentException.class,
                () -> config.setupSoot(CLASS, false, "does-not-exist"));

        config.setCallGraphAlgorithm("unknown");
        assertThrows(IllegalArgumentException.class,
                () -> config.setupSoot(CLASS, true, CLASSES));
    }
}
