package TestCaseDroid.analysis.reachability;

import TestCaseDroid.config.SootConfig;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import soot.Scene;
import soot.jimple.toolkits.callgraph.CallGraph;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ReachabilityCGTest {
    private static final String CLASS = "TestCaseDroid.test.callgraph.CallGraphExamples";
    private static final String CLASSES = Paths.get("target", "classes").toAbsolutePath().toString();
    private CallGraph callGraph;

    @BeforeAll
    void buildCallGraphOnce() {
        new SootConfig().setupSoot(CLASS, true, CLASSES, Arrays.asList(
                signature("diamondEntry"),
                signature("recursiveEntry"),
                signature("overloadEntry"),
                signature("interfaceEntry")));
        callGraph = Scene.v().getCallGraph();
    }

    @Test
    void preservesAllDiamondPathsInsteadOfGloballyMarkingSharedNodesVisited() {
        ReachabilityCG analysis = analysis("diamondEntry", "diamondSink");
        List<MethodContext> paths = analysis.analyzeCallGraph(
                analysis.getSourceMethodContext(), analysis.getTargetMethodContext());

        assertEquals(3, paths.size());
        List<Integer> lengths = new ArrayList<>();
        for (MethodContext path : paths) {
            lengths.add(path.getMethodCallStack().size());
        }
        assertTrue(lengths.contains(3), "direct path should contain entry, direct and sink");
        assertEquals(2, lengths.stream().filter(length -> length == 4).count(),
                "left and right paths should remain distinct after merging");
    }

    @Test
    void terminatesOnRecursiveCyclesAndStillFindsTheExit() {
        ReachabilityCG analysis = analysis("recursiveEntry", "recursiveSink");
        List<MethodContext> paths = analysis.analyzeCallGraph(
                analysis.getSourceMethodContext(), analysis.getTargetMethodContext());

        assertEquals(1, paths.size());
        assertEquals(4, paths.get(0).getMethodCallStack().size());
    }

    @Test
    void distinguishesOverloadsAndReportsUnreachableMethods() {
        assertTrue(analysis("overloadEntry", "intOverloadSink").isReachable());
        assertFalse(analysis("overloadEntry", "stringOverloadSink").isReachable());
        assertFalse(analysis("diamondEntry", "unreachableSink").isReachable());
    }

    @Test
    void resolvesInterfaceDispatchWithCha() {
        assertTrue(analysis("interfaceEntry", "interfaceSink").isReachable());
    }

    @Test
    void honorsPerPathDepthBound() {
        ReachabilityCG analysis = analysis("diamondEntry", "diamondSink");
        analysis.setMaxDepth(1);
        assertFalse(analysis.isReachable());
        analysis.setMaxDepth(2);
        assertTrue(analysis.isReachable());
    }

    private ReachabilityCG analysis(String sourceName, String targetName) {
        return new ReachabilityCG(callGraph,
                new MethodContext(signature(sourceName)),
                new MethodContext(signature(targetName)));
    }

    private static String signature(String methodName) {
        String parameters = "cfgEntry".equals(methodName) ? "int" : "";
        return "<" + CLASS + ": void " + methodName + "(" + parameters + ")>";
    }
}
