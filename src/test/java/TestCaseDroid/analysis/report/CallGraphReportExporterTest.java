package TestCaseDroid.analysis.report;

import com.alibaba.fastjson.JSON;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CallGraphReportExporterTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    @SuppressWarnings("unchecked")
    void detailLevelsExposeProgressivelyRicherEvidence() {
        CallGraphSnapshot snapshot = GraphViewBuilderTest.fixture();
        Map<String, Object> summary = CallGraphReportExporter.createReport(
                snapshot, CallGraphGranularity.METHOD,
                CallGraphDetailLevel.SUMMARY);
        Map<String, Object> detailed = CallGraphReportExporter.createReport(
                snapshot, CallGraphGranularity.CALL_SITE,
                CallGraphDetailLevel.DETAILED);

        List<Map<String, Object>> summaryNodes =
                (List<Map<String, Object>>) ((Map<String, Object>)
                        summary.get("graph")).get("nodes");
        List<Map<String, Object>> detailedNodes =
                (List<Map<String, Object>>) ((Map<String, Object>)
                        detailed.get("graph")).get("nodes");

        assertFalse(summaryNodes.get(0).containsKey("details"));
        assertTrue(detailedNodes.stream().anyMatch(
                node -> "call-site".equals(node.get("kind"))
                        && node.containsKey("details")));
        assertEquals(CallGraphReportExporter.SCHEMA_VERSION,
                detailed.get("schemaVersion"));
    }

    @Test
    void writesUtf8JsonAndMarkdownAiBundle() throws Exception {
        List<CallGraphReportExporter.ExportResult> results =
                CallGraphReportExporter.writeAiBundle(
                        temporaryDirectory, GraphViewBuilderTest.fixture());

        assertEquals(3, results.size());
        for (CallGraphReportExporter.ExportResult result : results) {
            assertTrue(Files.size(result.getJsonPath()) > 0);
            assertTrue(Files.size(result.getMarkdownPath()) > 0);
            String markdown = new String(
                    Files.readAllBytes(result.getMarkdownPath()),
                    StandardCharsets.UTF_8);
            assertTrue(markdown.contains("TestCaseDroid call-graph context"));
            assertFalse(markdown.contains("\uFFFD"));
            JSON.parseObject(new String(
                    Files.readAllBytes(result.getJsonPath()),
                    StandardCharsets.UTF_8));
        }
    }
}
