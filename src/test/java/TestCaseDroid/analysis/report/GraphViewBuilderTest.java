package TestCaseDroid.analysis.report;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GraphViewBuilderTest {
    @Test
    void buildsPackageClassMethodAndCallSiteViews() {
        CallGraphSnapshot snapshot = fixture();

        GraphView packages = GraphViewBuilder.build(
                snapshot, CallGraphGranularity.PACKAGE);
        GraphView classes = GraphViewBuilder.build(
                snapshot, CallGraphGranularity.CLASS);
        GraphView methods = GraphViewBuilder.build(
                snapshot, CallGraphGranularity.METHOD);
        GraphView callSites = GraphViewBuilder.build(
                snapshot, CallGraphGranularity.CALL_SITE);

        assertEquals(2, packages.getNodes().size());
        assertEquals(3, classes.getNodes().size());
        assertEquals(4, methods.getNodes().size());
        assertEquals(8, callSites.getNodes().size());
        assertEquals(8, callSites.getEdges().size());
        assertTrue(classes.getEdges().stream().anyMatch(edge -> edge.getCount() == 2));
    }

    @Test
    void appliesDepthAndLibraryFiltersWithoutDanglingEdges() {
        GraphView view = GraphViewBuilder.build(
                fixture(), CallGraphGranularity.METHOD, 1, false, false);

        assertEquals(2, view.getNodes().size());
        assertEquals(1, view.getEdges().size());
        assertTrue(view.getEdges().stream().allMatch(edge ->
                view.findNode(edge.getSource()) != null
                        && view.findNode(edge.getTarget()) != null));
    }

    public static CallGraphSnapshot fixture() {
        List<CallGraphSnapshot.MethodNode> nodes = new ArrayList<>();
        nodes.add(node("<sample.Entry: void start()>", "sample", "sample.Entry",
                "start", 0, true, false));
        nodes.add(node("<sample.Work: void run()>", "sample", "sample.Work",
                "run", 1, true, false));
        CallGraphSnapshot.MethodNode loop = node(
                "<sample.Work: void loop()>", "sample", "sample.Work",
                "loop", 2, true, false);
        loop.setRecursive(true);
        nodes.add(loop);
        nodes.add(node("<java.io.PrintStream: void println()>", "java.io",
                "java.io.PrintStream", "println", 2, false, true));

        List<CallGraphSnapshot.CallEdge> edges = Arrays.asList(
                edge("e1", nodes.get(0), nodes.get(1), 10, "run()"),
                edge("e2", nodes.get(1), nodes.get(2), 20, "loop()"),
                edge("e3", nodes.get(2), nodes.get(2), 30, "loop()"),
                edge("e4", nodes.get(1), nodes.get(3), 21, "println()"));
        return new CallGraphSnapshot(
                new CallGraphSnapshot.Metadata(
                        "sample.Entry", nodes.get(0).getId(), "CHA",
                        "target/classes", 12, 2000, true, true),
                nodes, edges,
                Collections.singletonList(Collections.singletonList(loop.getId())),
                Collections.singletonList("<sample.Dead: void unused()>"),
                Collections.singletonList(
                        "Static call graphs may over-approximate runtime dispatch."),
                false);
    }

    private static CallGraphSnapshot.MethodNode node(
            String id, String packageName, String className,
            String methodName, int depth, boolean application, boolean library) {
        return new CallGraphSnapshot.MethodNode(
                id, packageName, className, methodName,
                "void " + methodName + "()", "void",
                Collections.emptyList(), "public static",
                application, library, false, true, depth, depth * 10);
    }

    private static CallGraphSnapshot.CallEdge edge(
            String id, CallGraphSnapshot.MethodNode source,
            CallGraphSnapshot.MethodNode target, int line, String statement) {
        return new CallGraphSnapshot.CallEdge(
                id, source.getId(), target.getId(), "STATIC",
                statement, line, source.isApplication(), target.isApplication());
    }
}
