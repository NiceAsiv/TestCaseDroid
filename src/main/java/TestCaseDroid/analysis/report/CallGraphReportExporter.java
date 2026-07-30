package TestCaseDroid.analysis.report;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Writes machine-readable JSON and compact Markdown context for AI systems.
 */
public final class CallGraphReportExporter {
    public static final String SCHEMA_VERSION = "testcasedroid.callgraph/1.0";

    private CallGraphReportExporter() {
    }

    public static ExportResult writeReport(Path outputDirectory,
                                           CallGraphSnapshot snapshot,
                                           CallGraphGranularity granularity,
                                           CallGraphDetailLevel detailLevel) throws IOException {
        Files.createDirectories(outputDirectory);
        String baseName = "callgraph-" + detailLevel.cliName()
                + "-" + granularity.cliName();
        Path jsonPath = outputDirectory.resolve(baseName + ".json");
        Path markdownPath = outputDirectory.resolve(baseName + ".md");
        Map<String, Object> report = createReport(snapshot, granularity, detailLevel);
        Files.write(jsonPath, JSON.toJSONString(report,
                SerializerFeature.PrettyFormat,
                SerializerFeature.WriteMapNullValue).getBytes(StandardCharsets.UTF_8));
        Files.write(markdownPath, toMarkdown(report).getBytes(StandardCharsets.UTF_8));
        return new ExportResult(jsonPath, markdownPath);
    }

    /**
     * Writes the recommended AI bundle: package summary, method standard and
     * call-site detailed reports.
     */
    public static List<ExportResult> writeAiBundle(Path outputDirectory,
                                                   CallGraphSnapshot snapshot) throws IOException {
        List<ExportResult> results = new ArrayList<>();
        results.add(writeReport(outputDirectory, snapshot,
                CallGraphGranularity.PACKAGE, CallGraphDetailLevel.SUMMARY));
        results.add(writeReport(outputDirectory, snapshot,
                CallGraphGranularity.METHOD, CallGraphDetailLevel.STANDARD));
        results.add(writeReport(outputDirectory, snapshot,
                CallGraphGranularity.CALL_SITE, CallGraphDetailLevel.DETAILED));
        return results;
    }

    public static Map<String, Object> createReport(CallGraphSnapshot snapshot,
                                                   CallGraphGranularity granularity,
                                                   CallGraphDetailLevel detailLevel) {
        GraphView view = GraphViewBuilder.build(snapshot, granularity);
        int nodeLimit = nodeLimit(detailLevel);
        int edgeLimit = edgeLimit(detailLevel);
        List<GraphView.Node> selectedNodes = first(view.getNodes(), nodeLimit);
        Set<String> selectedNodeIds = new HashSet<>();
        for (GraphView.Node node : selectedNodes) {
            selectedNodeIds.add(node.getId());
        }
        List<GraphView.Edge> candidateEdges = new ArrayList<>();
        for (GraphView.Edge edge : view.getEdges()) {
            if (selectedNodeIds.contains(edge.getSource())
                    && selectedNodeIds.contains(edge.getTarget())) {
                candidateEdges.add(edge);
            }
        }
        List<GraphView.Edge> selectedEdges = first(candidateEdges, edgeLimit);

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("schemaVersion", SCHEMA_VERSION);
        report.put("generatedAt", Instant.now().toString());
        report.put("detailLevel", detailLevel.cliName());
        report.put("granularity", granularity.cliName());
        report.put("analysis", analysisMetadata(snapshot));
        report.put("metrics", metrics(snapshot, view));
        report.put("coverage", coverage(snapshot, view, selectedNodes, selectedEdges));
        report.put("insights", insights(snapshot, view, detailLevel));
        report.put("warnings", snapshot.getWarnings());
        report.put("aiUsage", aiUsage(detailLevel, granularity));

        Map<String, Object> graph = new LinkedHashMap<>();
        List<Map<String, Object>> nodes = new ArrayList<>();
        for (GraphView.Node node : selectedNodes) {
            nodes.add(nodeMap(node, detailLevel));
        }
        List<Map<String, Object>> edges = new ArrayList<>();
        for (GraphView.Edge edge : selectedEdges) {
            edges.add(edgeMap(edge, detailLevel));
        }
        graph.put("nodes", nodes);
        graph.put("edges", edges);
        report.put("graph", graph);
        return report;
    }

    public static String nodeContext(GraphView view,
                                     String nodeId,
                                     CallGraphDetailLevel detailLevel) {
        GraphView.Node selected = view.findNode(nodeId);
        if (selected == null) {
            return "No node selected.";
        }
        StringBuilder result = new StringBuilder();
        result.append("# Selected call-graph node\n\n");
        result.append("- ID: `").append(selected.getId()).append("`\n");
        result.append("- Label: ").append(selected.getLabel()).append("\n");
        result.append("- Kind: ").append(selected.getKind()).append("\n");
        result.append("- Depth: ").append(selected.getDepth()).append("\n");
        result.append("- Fan-in / fan-out: ").append(selected.getFanIn())
                .append(" / ").append(selected.getFanOut()).append("\n");
        result.append("- Recursive: ").append(selected.isRecursive()).append("\n");
        if (detailLevel != CallGraphDetailLevel.SUMMARY) {
            for (Map.Entry<String, Object> entry : selected.getDetails().entrySet()) {
                result.append("- ").append(entry.getKey()).append(": ")
                        .append(String.valueOf(entry.getValue())).append("\n");
            }
        }

        result.append("\n## Incoming calls\n\n");
        int incoming = 0;
        for (GraphView.Edge edge : view.getEdges()) {
            if (edge.getTarget().equals(nodeId)) {
                result.append("- `").append(edge.getSource()).append("`")
                        .append(edgeEvidence(edge, detailLevel)).append("\n");
                incoming++;
            }
        }
        if (incoming == 0) {
            result.append("- None in the displayed graph.\n");
        }
        result.append("\n## Outgoing calls\n\n");
        int outgoing = 0;
        for (GraphView.Edge edge : view.getEdges()) {
            if (edge.getSource().equals(nodeId)) {
                result.append("- `").append(edge.getTarget()).append("`")
                        .append(edgeEvidence(edge, detailLevel)).append("\n");
                outgoing++;
            }
        }
        if (outgoing == 0) {
            result.append("- None in the displayed graph.\n");
        }
        return result.toString();
    }

    @SuppressWarnings("unchecked")
    public static String toMarkdown(Map<String, Object> report) {
        Map<String, Object> analysis = (Map<String, Object>) report.get("analysis");
        Map<String, Object> metrics = (Map<String, Object>) report.get("metrics");
        Map<String, Object> coverage = (Map<String, Object>) report.get("coverage");
        Map<String, Object> insights = (Map<String, Object>) report.get("insights");
        Map<String, Object> graph = (Map<String, Object>) report.get("graph");
        List<Map<String, Object>> nodes = (List<Map<String, Object>>) graph.get("nodes");
        List<Map<String, Object>> edges = (List<Map<String, Object>>) graph.get("edges");

        StringBuilder result = new StringBuilder();
        result.append("# TestCaseDroid call-graph context\n\n");
        result.append("> Schema `").append(report.get("schemaVersion"))
                .append("`; detail `").append(report.get("detailLevel"))
                .append("`; granularity `").append(report.get("granularity")).append("`.\n\n");
        result.append("## Analysis scope\n\n");
        result.append("- Entry: `").append(analysis.get("entryMethod")).append("`\n");
        result.append("- Algorithm: ").append(analysis.get("algorithm")).append("\n");
        result.append("- Method snapshot: ").append(metrics.get("methodNodes"))
                .append(" nodes, ").append(metrics.get("methodEdges")).append(" edges\n");
        result.append("- Display graph: ").append(metrics.get("viewNodes"))
                .append(" nodes, ").append(metrics.get("viewEdges")).append(" edges\n");
        result.append("- Report coverage: ").append(coverage.get("nodesIncluded"))
                .append("/").append(coverage.get("nodesAvailable")).append(" nodes, ")
                .append(coverage.get("edgesIncluded")).append("/")
                .append(coverage.get("edgesAvailable")).append(" edges\n");
        result.append("- Analysis truncated: ").append(analysis.get("truncated")).append("\n\n");

        result.append("## Key findings\n\n");
        appendFinding(result, "Recursive components", insights.get("recursiveComponents"));
        appendFinding(result, "Highest fan-in", insights.get("highestFanIn"));
        appendFinding(result, "Highest fan-out", insights.get("highestFanOut"));
        appendFinding(result, "Entry nodes", insights.get("entryNodes"));
        appendFinding(result, "Leaf nodes", insights.get("leafNodes"));
        appendFinding(result, "Unreachable application methods",
                insights.get("unreachableApplicationMethods"));

        result.append("\n## Nodes\n\n");
        result.append("| ID | Label | Kind | Depth | In | Out | Recursive |\n");
        result.append("| --- | --- | --- | ---: | ---: | ---: | --- |\n");
        for (Map<String, Object> node : nodes) {
            result.append("| `").append(escape(node.get("id"))).append("` | ")
                    .append(escape(node.get("label"))).append(" | ")
                    .append(node.get("kind")).append(" | ")
                    .append(node.get("depth")).append(" | ")
                    .append(node.get("fanIn")).append(" | ")
                    .append(node.get("fanOut")).append(" | ")
                    .append(node.get("recursive")).append(" |\n");
        }

        result.append("\n## Edges\n\n");
        result.append("| Source | Target | Kind | Calls | Evidence |\n");
        result.append("| --- | --- | --- | ---: | --- |\n");
        for (Map<String, Object> edge : edges) {
            Object evidence = edge.containsKey("callSite")
                    ? edge.get("callSite") : edge.get("sourceLine");
            result.append("| `").append(escape(edge.get("source"))).append("` | `")
                    .append(escape(edge.get("target"))).append("` | ")
                    .append(escape(edge.get("kind"))).append(" | ")
                    .append(edge.get("count")).append(" | ")
                    .append(escape(evidence)).append(" |\n");
        }

        result.append("\n## Interpretation warnings\n\n");
        for (Object warning : (List<?>) report.get("warnings")) {
            result.append("- ").append(warning).append("\n");
        }
        result.append("\n").append(report.get("aiUsage")).append("\n");
        return result.toString();
    }

    private static Map<String, Object> analysisMetadata(CallGraphSnapshot snapshot) {
        CallGraphSnapshot.Metadata metadata = snapshot.getMetadata();
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("entryClass", metadata.getEntryClass());
        result.put("entryMethod", metadata.getEntryMethod());
        result.put("algorithm", metadata.getAlgorithm());
        result.put("classPath", metadata.getClassPath());
        result.put("maxDepth", metadata.getMaxDepth());
        result.put("maxNodes", metadata.getMaxNodes());
        result.put("includesLibraries", metadata.isIncludesLibraries());
        result.put("includesConstructors", metadata.isIncludesConstructors());
        result.put("truncated", snapshot.isTruncated());
        return result;
    }

    private static Map<String, Object> metrics(CallGraphSnapshot snapshot, GraphView view) {
        Set<String> packages = new HashSet<>();
        Set<String> classes = new HashSet<>();
        int applicationMethods = 0;
        int libraryMethods = 0;
        for (CallGraphSnapshot.MethodNode method : snapshot.getNodes()) {
            packages.add(method.getPackageName());
            classes.add(method.getClassName());
            if (method.isApplication()) {
                applicationMethods++;
            } else {
                libraryMethods++;
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("packages", packages.size());
        result.put("classes", classes.size());
        result.put("methodNodes", snapshot.getNodes().size());
        result.put("methodEdges", snapshot.getEdges().size());
        result.put("applicationMethods", applicationMethods);
        result.put("libraryMethods", libraryMethods);
        result.put("viewNodes", view.getNodes().size());
        result.put("viewEdges", view.getEdges().size());
        result.put("recursiveComponents", snapshot.getRecursiveComponents().size());
        result.put("unreachableApplicationMethods",
                snapshot.getUnreachableApplicationMethods().size());
        return result;
    }

    private static Map<String, Object> coverage(CallGraphSnapshot snapshot,
                                                GraphView view,
                                                List<GraphView.Node> nodes,
                                                List<GraphView.Edge> edges) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("nodesAvailable", view.getNodes().size());
        result.put("nodesIncluded", nodes.size());
        result.put("nodesOmitted", view.getNodes().size() - nodes.size());
        result.put("edgesAvailable", view.getEdges().size());
        result.put("edgesIncluded", edges.size());
        result.put("edgesOmitted", view.getEdges().size() - edges.size());
        result.put("analysisTruncated", snapshot.isTruncated());
        result.put("completeForDisplayedScope",
                !snapshot.isTruncated()
                        && nodes.size() == view.getNodes().size()
                        && edges.size() == view.getEdges().size());
        return result;
    }

    private static Map<String, Object> insights(CallGraphSnapshot snapshot,
                                                GraphView view,
                                                CallGraphDetailLevel detailLevel) {
        int sampleSize = detailLevel == CallGraphDetailLevel.SUMMARY ? 5
                : detailLevel == CallGraphDetailLevel.STANDARD ? 20 : 100;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("entryNodes", nodeIds(filterByDepth(view.getNodes(), 0), sampleSize));
        result.put("leafNodes", nodeIds(filterLeaves(view.getNodes()), sampleSize));
        result.put("highestFanIn", rankedNodes(view.getNodes(), true, sampleSize));
        result.put("highestFanOut", rankedNodes(view.getNodes(), false, sampleSize));
        result.put("recursiveComponents",
                first(snapshot.getRecursiveComponents(), sampleSize));
        result.put("unreachableApplicationMethods",
                first(snapshot.getUnreachableApplicationMethods(), sampleSize));
        return result;
    }

    private static List<GraphView.Node> filterByDepth(List<GraphView.Node> nodes, int depth) {
        List<GraphView.Node> result = new ArrayList<>();
        for (GraphView.Node node : nodes) {
            if (node.getDepth() == depth) {
                result.add(node);
            }
        }
        return result;
    }

    private static List<GraphView.Node> filterLeaves(List<GraphView.Node> nodes) {
        List<GraphView.Node> result = new ArrayList<>();
        for (GraphView.Node node : nodes) {
            if (node.getFanOut() == 0) {
                result.add(node);
            }
        }
        return result;
    }

    private static List<String> nodeIds(List<GraphView.Node> nodes, int limit) {
        List<String> result = new ArrayList<>();
        for (GraphView.Node node : first(nodes, limit)) {
            result.add(node.getId());
        }
        return result;
    }

    private static List<Map<String, Object>> rankedNodes(List<GraphView.Node> nodes,
                                                         boolean fanIn,
                                                         int limit) {
        List<GraphView.Node> ranked = new ArrayList<>(nodes);
        ranked.sort((left, right) -> {
            int leftMetric = fanIn ? left.getFanIn() : left.getFanOut();
            int rightMetric = fanIn ? right.getFanIn() : right.getFanOut();
            int comparison = Integer.compare(rightMetric, leftMetric);
            return comparison != 0 ? comparison : left.getId().compareTo(right.getId());
        });
        List<Map<String, Object>> result = new ArrayList<>();
        for (GraphView.Node node : first(ranked, limit)) {
            int metric = fanIn ? node.getFanIn() : node.getFanOut();
            if (metric == 0) {
                break;
            }
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", node.getId());
            item.put("label", node.getLabel());
            item.put(fanIn ? "fanIn" : "fanOut", metric);
            result.add(item);
        }
        return result;
    }

    private static Map<String, Object> nodeMap(GraphView.Node node,
                                               CallGraphDetailLevel detailLevel) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("id", node.getId());
        result.put("label", node.getLabel());
        result.put("kind", node.getKind());
        result.put("depth", node.getDepth());
        result.put("fanIn", node.getFanIn());
        result.put("fanOut", node.getFanOut());
        result.put("recursive", node.isRecursive());
        if (detailLevel != CallGraphDetailLevel.SUMMARY) {
            result.put("group", node.getGroup());
            result.put("application", node.isApplication());
            result.put("library", node.isLibrary());
        }
        if (detailLevel == CallGraphDetailLevel.DETAILED) {
            result.put("constructor", node.isConstructor());
            result.put("details", node.getDetails());
        } else if (detailLevel == CallGraphDetailLevel.STANDARD) {
            Map<String, Object> selectedDetails = new LinkedHashMap<>();
            copyIfPresent(node.getDetails(), selectedDetails, "signature");
            copyIfPresent(node.getDetails(), selectedDetails, "class");
            copyIfPresent(node.getDetails(), selectedDetails, "method");
            copyIfPresent(node.getDetails(), selectedDetails, "methodCount");
            result.put("details", selectedDetails);
        }
        return result;
    }

    private static Map<String, Object> edgeMap(GraphView.Edge edge,
                                               CallGraphDetailLevel detailLevel) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("source", edge.getSource());
        result.put("target", edge.getTarget());
        result.put("count", edge.getCount());
        if (detailLevel != CallGraphDetailLevel.SUMMARY) {
            result.put("kind", edge.getKind());
            result.put("label", edge.getLabel());
        } else {
            result.put("kind", edge.getKind());
        }
        if (detailLevel == CallGraphDetailLevel.DETAILED) {
            result.put("id", edge.getId());
            result.put("sourceLine", edge.getSourceLine());
            result.put("callSite", edge.getCallSite());
        }
        return result;
    }

    private static void copyIfPresent(Map<String, Object> source,
                                      Map<String, Object> target,
                                      String key) {
        if (source.containsKey(key)) {
            target.put(key, source.get(key));
        }
    }

    private static String aiUsage(CallGraphDetailLevel detailLevel,
                                  CallGraphGranularity granularity) {
        switch (detailLevel) {
            case SUMMARY:
                return "AI guidance: use this " + granularity.cliName()
                        + " overview to identify architectural areas and choose a narrower follow-up report.";
            case DETAILED:
                return "AI guidance: treat call-site statements and source lines as static-analysis evidence; "
                        + "confirm dynamic dispatch and reflection against runtime behaviour.";
            case STANDARD:
            default:
                return "AI guidance: use signatures and directed edges for impact/reachability reasoning, "
                        + "then request the detailed call-site report for exact evidence.";
        }
    }

    private static String edgeEvidence(GraphView.Edge edge,
                                       CallGraphDetailLevel detailLevel) {
        if (detailLevel == CallGraphDetailLevel.SUMMARY) {
            return " (" + edge.getCount() + " call(s))";
        }
        if (detailLevel == CallGraphDetailLevel.DETAILED) {
            return " (" + edge.getKind() + ", line " + edge.getSourceLine()
                    + ", `" + edge.getCallSite() + "`)";
        }
        return " (" + edge.getKind() + ", " + edge.getCount() + " call(s))";
    }

    private static int nodeLimit(CallGraphDetailLevel detailLevel) {
        switch (detailLevel) {
            case SUMMARY:
                return 80;
            case STANDARD:
                return 800;
            case DETAILED:
            default:
                return 10000;
        }
    }

    private static int edgeLimit(CallGraphDetailLevel detailLevel) {
        switch (detailLevel) {
            case SUMMARY:
                return 160;
            case STANDARD:
                return 1600;
            case DETAILED:
            default:
                return 20000;
        }
    }

    private static <T> List<T> first(List<T> values, int limit) {
        if (values.size() <= limit) {
            return new ArrayList<>(values);
        }
        return new ArrayList<>(values.subList(0, limit));
    }

    private static void appendFinding(StringBuilder result, String label, Object value) {
        result.append("- ").append(label).append(": ").append(String.valueOf(value)).append("\n");
    }

    private static String escape(Object value) {
        if (value == null) {
            return "";
        }
        return String.valueOf(value)
                .replace("|", "\\|")
                .replace("\r", " ")
                .replace("\n", " ");
    }

    public static final class ExportResult {
        private final Path jsonPath;
        private final Path markdownPath;

        public ExportResult(Path jsonPath, Path markdownPath) {
            this.jsonPath = jsonPath;
            this.markdownPath = markdownPath;
        }

        public Path getJsonPath() {
            return jsonPath;
        }

        public Path getMarkdownPath() {
            return markdownPath;
        }
    }
}
