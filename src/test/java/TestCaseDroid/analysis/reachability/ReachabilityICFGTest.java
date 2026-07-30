package TestCaseDroid.analysis.reachability;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import soot.Scene;
import soot.SootMethod;

import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReachabilityICFGTest {
    private static final String CLASS = "TestCaseDroid.test.callgraph.CallGraphExamples";
    private static final String CLASSES = Paths.get("target", "classes").toAbsolutePath().toString();
    private ReachabilityICFG analysis;

    @BeforeAll
    void setUpAnalysis() {
        analysis = new ReachabilityICFG(CLASS, CLASSES);
    }

    @Test
    void followsCallsAndReturnsToReachTargetMethod() {
        List<Context> paths = analysis.inDynamicExtent(
                method("diamondEntry"), method("diamondSink"));

        assertFalse(paths.isEmpty());
        assertTrue(paths.stream().allMatch(path -> path.getReachedMethod().equals(method("diamondSink"))));
    }

    @Test
    void returnsEmptyListForUnreachableTarget() {
        assertTrue(analysis.inDynamicExtent(
                method("diamondEntry"), method("unreachableSink")).isEmpty());
    }

    @Test
    void recursionIsBoundedAndCanReachAnExit() {
        analysis.setMaxDepth(100);
        assertFalse(analysis.inDynamicExtent(
                method("recursiveEntry"), method("recursiveSink")).isEmpty());
    }

    private static SootMethod method(String name) {
        return Scene.v().getMethod("<" + CLASS + ": void " + name + "()>");
    }
}
