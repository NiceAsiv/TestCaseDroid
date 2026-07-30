package TestCaseDroid.analysis.reachability;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import soot.Scene;
import soot.SootMethod;

import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReachabilityCFGTest {
    private static final String CLASS = "TestCaseDroid.test.callgraph.CallGraphExamples";
    private static final String CLASSES = Paths.get("target", "classes").toAbsolutePath().toString();
    private static final String SOURCE = "<" + CLASS + ": void cfgEntry(int)>";
    private ReachabilityCFG analysis;

    @BeforeAll
    void setUpAnalysis() {
        analysis = new ReachabilityCFG(CLASS, signature("cfgSink"), SOURCE, CLASSES);
    }

    @Test
    void findsRealBranchPathToInvocationAfterLoop() {
        SootMethod target = Scene.v().getMethod(signature("cfgSink"));
        List<Context> paths = analysis.inDynamicExtent(target);

        assertFalse(paths.isEmpty());
        assertTrue(paths.stream().allMatch(
                path -> analysis.isTargetNode(path.getReachedNode(), target)));
    }

    @Test
    void doesNotReportInvocationFromOtherMethodAsIntraMethodReachable() {
        assertTrue(analysis.inDynamicExtent(
                Scene.v().getMethod(signature("unreachableSink"))).isEmpty());
    }

    @Test
    void generatedPathGraphsCorrespondOneToOneWithWitnesses() {
        int witnesses = analysis.inDynamicExtent(
                Scene.v().getMethod(signature("cfgSink"))).size();
        assertEquals(witnesses, analysis.markNodeForReachable().size());
    }

    private static String signature(String name) {
        return "<" + CLASS + ": void " + name + "()>";
    }
}
