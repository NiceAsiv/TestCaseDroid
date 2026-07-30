package TestCaseDroid.visualization;

import TestCaseDroid.analysis.report.CallGraphGranularity;
import TestCaseDroid.analysis.report.GraphView;
import TestCaseDroid.analysis.report.GraphViewBuilder;
import TestCaseDroid.analysis.report.GraphViewBuilderTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CallGraphCanvasTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void rendersGraphToPngInHeadlessMode() throws Exception {
        System.setProperty("java.awt.headless", "true");
        GraphView view = GraphViewBuilder.build(
                GraphViewBuilderTest.fixture(), CallGraphGranularity.METHOD);
        CallGraphCanvas canvas = new CallGraphCanvas();
        canvas.setGraphView(view);
        canvas.setSelectedNode(view.getNodes().get(0).getId());
        canvas.setSearchText("loop");

        Path output = temporaryDirectory.resolve("callgraph.png");
        canvas.writePng(output);

        assertTrue(Files.size(output) > 1000);
        byte[] bytes = Files.readAllBytes(output);
        assertArrayEquals(new byte[]{
                (byte) 0x89, 0x50, 0x4E, 0x47,
                0x0D, 0x0A, 0x1A, 0x0A
        }, java.util.Arrays.copyOf(bytes, 8));
    }
}
