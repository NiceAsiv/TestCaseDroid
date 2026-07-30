package TestCaseDroid.analysis.report;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CallGraphAnalyzerTest {
    private static final String CLASS =
            "TestCaseDroid.test.callgraph.CallGraphExamples";
    private static final String CLASSES =
            new File("target/classes").getAbsolutePath();

    @Test
    void extractsEveryBranchOfDiamondGraph() {
        CallGraphSnapshot snapshot = analyze("diamondEntry");
        List<String> ids = snapshot.getNodes().stream()
                .map(CallGraphSnapshot.MethodNode::getId)
                .collect(Collectors.toList());

        assertEquals(6, snapshot.getNodes().size());
        assertEquals(7, snapshot.getEdges().size());
        assertTrue(ids.stream().anyMatch(id -> id.contains("void direct()")));
        assertTrue(ids.stream().anyMatch(id -> id.contains("void left()")));
        assertTrue(ids.stream().anyMatch(id -> id.contains("void right()")));
        assertTrue(ids.stream().anyMatch(id -> id.contains("void shared()")));
        assertTrue(ids.stream().anyMatch(id -> id.contains("void diamondSink()")));
        assertFalse(snapshot.isTruncated());
    }

    @Test
    void identifiesMutualRecursionAndExit() {
        CallGraphSnapshot snapshot = analyze("recursiveEntry");

        assertEquals(1, snapshot.getRecursiveComponents().size());
        List<String> component = snapshot.getRecursiveComponents().get(0);
        assertEquals(2, component.size());
        assertTrue(component.stream().anyMatch(id -> id.contains("recursiveA(int)")));
        assertTrue(component.stream().anyMatch(id -> id.contains("recursiveB(int)")));
        assertTrue(snapshot.getNodes().stream()
                .anyMatch(node -> node.getMethodName().equals("recursiveSink")));
    }

    @Test
    void reportsMethodsOutsideTheSelectedEntryGraph() {
        CallGraphSnapshot snapshot = analyze("overloadEntry");

        assertTrue(snapshot.getUnreachableApplicationMethods().stream()
                .anyMatch(id -> id.contains("void overloaded(java.lang.String)")));
        assertTrue(snapshot.getUnreachableApplicationMethods().stream()
                .anyMatch(id -> id.contains("void stringOverloadSink()")));
        assertTrue(snapshot.getNodes().stream()
                .anyMatch(node -> node.getId().contains("void overloaded(int)")));
    }

    private static CallGraphSnapshot analyze(String method) {
        return CallGraphAnalyzer.analyze(
                CLASS,
                "<" + CLASS + ": void " + method + "()>",
                CLASSES,
                "CHA",
                new CallGraphAnalysisOptions()
                        .setMaxDepth(10)
                        .setMaxNodes(100));
    }
}
