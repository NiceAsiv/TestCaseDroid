package TestCaseDroid.analysis.reachability;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import soot.Scene;
import soot.SootMethod;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BackwardReachabilityICFGTest {
    private static final String CLASS = "TestCaseDroid.test.callgraph.CallGraphExamples";
    private static final String CLASSES = Paths.get("target", "classes").toAbsolutePath().toString();
    private BackwardReachabilityICFG analysis;

    @BeforeAll
    void setUpAnalysis() {
        analysis = new BackwardReachabilityICFG(CLASS, CLASSES);
    }

    @Test
    void findsCallerChainFromTargetBackToSource() {
        assertFalse(analysis.inDynamicExtent(
                method("recursiveEntry"), method("recursiveSink")).isEmpty());
    }

    @Test
    void returnsEmptyListWhenSourceIsNotAnAncestor() {
        assertTrue(analysis.inDynamicExtent(
                method("overloadEntry"), method("diamondSink")).isEmpty());
    }

    private static SootMethod method(String name) {
        return Scene.v().getMethod("<" + CLASS + ": void " + name + "()>");
    }
}
