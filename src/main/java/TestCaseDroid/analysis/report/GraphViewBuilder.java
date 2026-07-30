package TestCaseDroid.analysis.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Converts a method snapshot into a graph at a selected structural level.
 */
public final class GraphViewBuilder {
    private GraphViewBuilder() {
    }

    public static GraphView build(CallGraphSnapshot snapshot,
                                  CallGraphGranularity granularity) {
        return build(snapshot, granularity, snapshot.getMetadata().getMaxDepth(),
                true, true);
    }

    public static GraphView build(CallGraphSnapshot snapshot,
                                  CallGraphGranularity granularity,
                                  int maxDepth,
                                  boolean includeLibraries,
                                  boolean includeConstructors) {
        switch (granularity) {
            case PACKAGE:
            case CLASS:
                return buildAggregated(snapshot, granularity, maxDepth,
                        includeLibraries, includeConstructors);
            case CALL_SITE:
                return buildCallSite(snapshot, maxDepth,
                        includeLibraries, includeConstructors);
            case METHOD:
            default:
                return buildMethods(snapshot, maxDepth,
                        includeLibraries, includeConstructors);
        }
    }

    private static GraphView buildMethods(CallGraphSnapshot snapshot,
                                          int maxDepth,
                                          boolean includeLibraries,
                                          boolean includeConstructors) {
        List<GraphView.Node> nodes = new ArrayList<>();
        Set<String> included = new HashSet<>();
        for (CallGraphSnapshot.MethodNode method : snapshot.getNodes()) {
            if (!isIncluded(method, maxDepth, includeLibraries, includeConstructors)) {
                continue;
            }
            included.add(method.getId());
            nodes.add(methodNode(method, method.getDepth()));
        }
        List<GraphView.Edge> edges = directEdges(snapshot, included);
        finish(nodes, edges, true);
        return new GraphView(CallGraphGranularity.METHOD, nodes, edges);
    }

    private static GraphView buildCallSite(CallGraphSnapshot snapshot,
                                           int maxDepth,
                                           boolean includeLibraries,
                                           boolean includeConstructors) {
        List<GraphView.Node> nodes = new ArrayList<>();
        Set<String> included = new HashSet<>();
        Map<String, CallGraphSnapshot.MethodNode> methodIndex = snapshot.nodeIndex();
        for (CallGraphSnapshot.MethodNode method : snapshot.getNodes()) {
            if (!isIncluded(method, maxDepth, includeLibraries, includeConstructors)) {
                continue;
            }
            included.add(method.getId());
            nodes.add(methodNode(method, method.getDepth() * 2));
        }

        List<GraphView.Edge> edges = new ArrayList<>();
        for (CallGraphSnapshot.CallEdge edge : snapshot.getEdges()) {
            if (!included.contains(edge.getSource()) || !included.contains(edge.getTarget())) {
                continue;
            }
            CallGraphSnapshot.MethodNode source = methodIndex.get(edge.getSource());
            String callSiteId = "callsite:" + edge.getId();
            Map<String, Object> details = new LinkedHashMap<>();
            details.put("sourceMethod", edge.getSource());
            details.put("targetMethod", edge.getTarget());
            details.put("dispatchKind", edge.getKind());
            details.put("sourceLine", edge.getSourceLine());
            details.put("statement", edge.getCallSite());
            String label = edge.getSourceLine() > 0
                    ? "call @ line " + edge.getSourceLine()
                    : "call site";
            nodes.add(new GraphView.Node(callSiteId, label, "call-site",
                    source == null ? "" : source.getClassName(),
                    source == null ? 1 : source.getDepth() * 2 + 1,
                    edge.isSourceApplication(), !edge.isSourceApplication(),
                    false, false, details));
            edges.add(new GraphView.Edge(
                    edge.getId() + ":source", edge.getSource(), callSiteId,
                    edge.getKind(), "invokes", 1, edge.getSourceLine(), edge.getCallSite()));
            edges.add(new GraphView.Edge(
                    edge.getId() + ":target", callSiteId, edge.getTarget(),
                    edge.getKind(), edge.getKind(), 1, edge.getSourceLine(), edge.getCallSite()));
        }
        finish(nodes, edges, true);
        return new GraphView(CallGraphGranularity.CALL_SITE, nodes, edges);
    }

    private static GraphView buildAggregated(CallGraphSnapshot snapshot,
                                             CallGraphGranularity granularity,
                                             int maxDepth,
                                             boolean includeLibraries,
                                             boolean includeConstructors) {
        Map<String, AggregateNode> aggregates = new LinkedHashMap<>();
        Map<String, String> methodToAggregate = new HashMap<>();
        for (CallGraphSnapshot.MethodNode method : snapshot.getNodes()) {
            if (!isIncluded(method, maxDepth, includeLibraries, includeConstructors)) {
                continue;
            }
            String key = granularity == CallGraphGranularity.PACKAGE
                    ? method.getPackageName() : method.getClassName();
            String id = granularity.cliName() + ":" + key;
            methodToAggregate.put(method.getId(), id);
            AggregateNode aggregate = aggregates.get(id);
            if (aggregate == null) {
                aggregate = new AggregateNode(id, key, granularity.cliName(),
                        granularity == CallGraphGranularity.PACKAGE
                                ? key : method.getPackageName(),
                        method.getDepth());
                aggregates.put(id, aggregate);
            }
            aggregate.add(method);
        }

        Map<String, AggregateEdge> aggregateEdges = new LinkedHashMap<>();
        for (CallGraphSnapshot.CallEdge edge : snapshot.getEdges()) {
            String source = methodToAggregate.get(edge.getSource());
            String target = methodToAggregate.get(edge.getTarget());
            if (source == null || target == null) {
                continue;
            }
            String key = source + " -> " + target;
            AggregateEdge aggregate = aggregateEdges.get(key);
            if (aggregate == null) {
                aggregate = new AggregateEdge(key, source, target);
                aggregateEdges.put(key, aggregate);
            }
            aggregate.add(edge);
        }

        List<GraphView.Node> nodes = new ArrayList<>();
        for (AggregateNode aggregate : aggregates.values()) {
            nodes.add(aggregate.toNode());
        }
        List<GraphView.Edge> edges = new ArrayList<>();
        for (AggregateEdge aggregate : aggregateEdges.values()) {
            edges.add(aggregate.toEdge());
        }
        finish(nodes, edges, false);
        return new GraphView(granularity, nodes, edges);
    }

    private static boolean isIncluded(CallGraphSnapshot.MethodNode method,
                                      int maxDepth,
                                      boolean includeLibraries,
                                      boolean includeConstructors) {
        return method.getDepth() <= maxDepth
                && (includeLibraries || !method.isLibrary())
                && (includeConstructors || !method.isConstructor());
    }

    private static GraphView.Node methodNode(CallGraphSnapshot.MethodNode method, int depth) {
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("signature", method.getId());
        details.put("package", method.getPackageName());
        details.put("class", method.getClassName());
        details.put("method", method.getMethodName());
        details.put("subSignature", method.getSubSignature());
        details.put("returnType", method.getReturnType());
        details.put("parameterTypes", method.getParameterTypes());
        details.put("modifiers", method.getModifiers());
        details.put("sourceLine", method.getSourceLine());
        details.put("concrete", method.isConcrete());
        return new GraphView.Node(method.getId(), method.getLabel(), "method",
                method.getClassName(), depth, method.isApplication(), method.isLibrary(),
                method.isConstructor(), method.isRecursive(), details);
    }

    private static List<GraphView.Edge> directEdges(CallGraphSnapshot snapshot,
                                                    Set<String> included) {
        List<GraphView.Edge> result = new ArrayList<>();
        for (CallGraphSnapshot.CallEdge edge : snapshot.getEdges()) {
            if (included.contains(edge.getSource()) && included.contains(edge.getTarget())) {
                result.add(new GraphView.Edge(
                        edge.getId(), edge.getSource(), edge.getTarget(), edge.getKind(),
                        edge.getKind(), 1, edge.getSourceLine(), edge.getCallSite()));
            }
        }
        return result;
    }

    private static void finish(List<GraphView.Node> nodes,
                               List<GraphView.Edge> edges,
                               boolean countSelfEdges) {
        nodes.sort(Comparator.comparingInt(GraphView.Node::getDepth)
                .thenComparing(GraphView.Node::getLabel)
                .thenComparing(GraphView.Node::getId));
        edges.sort(Comparator.comparing(GraphView.Edge::getSource)
                .thenComparing(GraphView.Edge::getTarget)
                .thenComparing(GraphView.Edge::getId));

        Map<String, GraphView.Node> index = new HashMap<>();
        Map<String, Set<String>> incoming = new HashMap<>();
        Map<String, Set<String>> outgoing = new HashMap<>();
        for (GraphView.Node node : nodes) {
            index.put(node.getId(), node);
        }
        for (GraphView.Edge edge : edges) {
            if (index.containsKey(edge.getSource()) && index.containsKey(edge.getTarget())) {
                if (!countSelfEdges && edge.getSource().equals(edge.getTarget())) {
                    continue;
                }
                outgoing.computeIfAbsent(edge.getSource(), ignored -> new HashSet<>())
                        .add(edge.getTarget());
                incoming.computeIfAbsent(edge.getTarget(), ignored -> new HashSet<>())
                        .add(edge.getSource());
            }
        }
        for (GraphView.Node node : nodes) {
            node.setFanIn(incoming.containsKey(node.getId())
                    ? incoming.get(node.getId()).size() : 0);
            node.setFanOut(outgoing.containsKey(node.getId())
                    ? outgoing.get(node.getId()).size() : 0);
        }
    }

    private static final class AggregateNode {
        private final String id;
        private final String label;
        private final String kind;
        private final String group;
        private final List<String> members = new ArrayList<>();
        private int depth;
        private int applicationMethods;
        private int libraryMethods;
        private int recursiveMethods;
        private boolean constructorOnly = true;

        private AggregateNode(String id, String label, String kind, String group, int depth) {
            this.id = id;
            this.label = label;
            this.kind = kind;
            this.group = group;
            this.depth = depth;
        }

        private void add(CallGraphSnapshot.MethodNode method) {
            members.add(method.getId());
            depth = Math.min(depth, method.getDepth());
            if (method.isApplication()) {
                applicationMethods++;
            } else {
                libraryMethods++;
            }
            if (method.isRecursive()) {
                recursiveMethods++;
            }
            if (!method.isConstructor()) {
                constructorOnly = false;
            }
        }

        private GraphView.Node toNode() {
            Collections.sort(members);
            Map<String, Object> details = new LinkedHashMap<>();
            details.put("methodCount", members.size());
            details.put("applicationMethodCount", applicationMethods);
            details.put("libraryMethodCount", libraryMethods);
            details.put("recursiveMethodCount", recursiveMethods);
            details.put("members", new ArrayList<>(members));
            return new GraphView.Node(id, label, kind, group, depth,
                    applicationMethods > 0, applicationMethods == 0,
                    constructorOnly, recursiveMethods > 0, details);
        }
    }

    private static final class AggregateEdge {
        private final String id;
        private final String source;
        private final String target;
        private final Map<String, Integer> kinds = new LinkedHashMap<>();
        private int count;

        private AggregateEdge(String id, String source, String target) {
            this.id = id;
            this.source = source;
            this.target = target;
        }

        private void add(CallGraphSnapshot.CallEdge edge) {
            count++;
            kinds.put(edge.getKind(), kinds.containsKey(edge.getKind())
                    ? kinds.get(edge.getKind()) + 1 : 1);
        }

        private GraphView.Edge toEdge() {
            String label = count + (count == 1 ? " call" : " calls");
            return new GraphView.Edge(id, source, target, "aggregate",
                    label, count, -1, kinds.toString());
        }
    }
}
