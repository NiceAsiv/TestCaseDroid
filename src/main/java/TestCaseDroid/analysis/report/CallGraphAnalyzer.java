package TestCaseDroid.analysis.report;

import TestCaseDroid.config.SootConfig;
import soot.Modifier;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;
import soot.Unit;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Extracts a bounded, deterministic call-graph snapshot from Soot.
 */
public final class CallGraphAnalyzer {
    private CallGraphAnalyzer() {
    }

    public static CallGraphSnapshot analyze(String entryClass,
                                            String entryMethodSignature,
                                            String classPath,
                                            String algorithm,
                                            CallGraphAnalysisOptions options) {
        if (entryMethodSignature == null || entryMethodSignature.trim().isEmpty()) {
            throw new IllegalArgumentException("An entry method signature is required");
        }
        CallGraphAnalysisOptions effectiveOptions =
                options == null ? new CallGraphAnalysisOptions() : options;

        SootConfig config = new SootConfig();
        config.setCallGraphAlgorithm(algorithm);
        config.setupSoot(entryClass, true, classPath,
                Collections.singletonList(entryMethodSignature));
        SootMethod entryMethod = Scene.v().getMethod(entryMethodSignature);
        return snapshot(entryClass, entryMethod, classPath, algorithm, effectiveOptions);
    }

    static CallGraphSnapshot snapshot(String entryClass,
                                      SootMethod entryMethod,
                                      String classPath,
                                      String algorithm,
                                      CallGraphAnalysisOptions options) {
        CallGraph callGraph = Scene.v().getCallGraph();
        Map<String, CallGraphSnapshot.MethodNode> nodes = new LinkedHashMap<>();
        List<CallGraphSnapshot.CallEdge> edges = new ArrayList<>();
        Set<String> edgeIds = new LinkedHashSet<>();
        Map<String, Integer> depths = new LinkedHashMap<>();
        Deque<SootMethod> queue = new ArrayDeque<>();
        boolean truncated = false;

        String entryId = entryMethod.getSignature();
        depths.put(entryId, 0);
        queue.add(entryMethod);

        while (!queue.isEmpty()) {
            SootMethod source = queue.removeFirst();
            String sourceId = source.getSignature();
            int depth = depths.get(sourceId);
            if (!nodes.containsKey(sourceId)) {
                nodes.put(sourceId, toNode(source, depth));
            }

            List<Edge> outgoing = outgoingEdges(callGraph, source);
            if (depth >= options.getMaxDepth()) {
                if (!outgoing.isEmpty()) {
                    truncated = true;
                }
                continue;
            }

            for (Edge edge : outgoing) {
                SootMethod target = edge.tgt();
                if (!isIncluded(target, options)) {
                    continue;
                }
                String targetId = target.getSignature();
                boolean known = depths.containsKey(targetId);
                if (!known && nodes.size() + queue.size() >= options.getMaxNodes()) {
                    truncated = true;
                    continue;
                }
                if (!known) {
                    depths.put(targetId, depth + 1);
                    queue.addLast(target);
                }

                int line = sourceLine(edge);
                String callSite = edge.srcUnit() == null ? "" : edge.srcUnit().toString();
                String kind = edge.kind() == null ? "UNKNOWN" : edge.kind().toString();
                String edgeId = sourceId + " -> " + targetId + " @"
                        + line + " [" + kind + "] " + callSite;
                if (edgeIds.add(edgeId)) {
                    edges.add(new CallGraphSnapshot.CallEdge(
                            edgeId, sourceId, targetId, kind, callSite, line,
                            source.getDeclaringClass().isApplicationClass(),
                            target.getDeclaringClass().isApplicationClass()));
                }
            }
        }

        edges.removeIf(edge -> !nodes.containsKey(edge.getSource())
                || !depths.containsKey(edge.getTarget()));
        for (Map.Entry<String, Integer> entry : depths.entrySet()) {
            if (!nodes.containsKey(entry.getKey())) {
                SootMethod method = Scene.v().getMethod(entry.getKey());
                nodes.put(entry.getKey(), toNode(method, entry.getValue()));
            }
        }

        List<CallGraphSnapshot.MethodNode> nodeList = new ArrayList<>(nodes.values());
        nodeList.sort(Comparator.comparingInt(CallGraphSnapshot.MethodNode::getDepth)
                .thenComparing(CallGraphSnapshot.MethodNode::getId));
        edges.sort(Comparator.comparing(CallGraphSnapshot.CallEdge::getSource)
                .thenComparing(CallGraphSnapshot.CallEdge::getTarget)
                .thenComparing(CallGraphSnapshot.CallEdge::getId));
        applyFanMetrics(nodes, edges);

        List<List<String>> recursiveComponents = findRecursiveComponents(nodes.keySet(), edges);
        for (List<String> component : recursiveComponents) {
            for (String methodId : component) {
                CallGraphSnapshot.MethodNode node = nodes.get(methodId);
                if (node != null) {
                    node.setRecursive(true);
                }
            }
        }

        List<String> unreachable = findUnreachableApplicationMethods(nodes.keySet(), options);
        List<String> warnings = buildWarnings(truncated);
        CallGraphSnapshot.Metadata metadata = new CallGraphSnapshot.Metadata(
                entryClass, entryMethod.getSignature(),
                algorithm == null ? "CHA" : algorithm.toUpperCase(),
                classPath, options.getMaxDepth(), options.getMaxNodes(),
                options.isIncludeLibraries(), options.isIncludeConstructors());

        return new CallGraphSnapshot(metadata, nodeList, edges,
                recursiveComponents, unreachable, warnings, truncated);
    }

    private static List<Edge> outgoingEdges(CallGraph callGraph, SootMethod method) {
        List<Edge> result = new ArrayList<>();
        Iterator<Edge> iterator = callGraph.edgesOutOf(method);
        while (iterator.hasNext()) {
            result.add(iterator.next());
        }
        result.sort(Comparator.comparing((Edge edge) -> edge.tgt().getSignature())
                .thenComparing(edge -> edge.kind() == null ? "" : edge.kind().toString())
                .thenComparing(edge -> edge.srcUnit() == null ? "" : edge.srcUnit().toString()));
        return result;
    }

    private static boolean isIncluded(SootMethod method, CallGraphAnalysisOptions options) {
        if (!options.isIncludeConstructors()
                && (method.isConstructor() || method.isStaticInitializer())) {
            return false;
        }
        return options.isIncludeLibraries()
                || method.getDeclaringClass().isApplicationClass();
    }

    private static CallGraphSnapshot.MethodNode toNode(SootMethod method, int depth) {
        SootClass declaringClass = method.getDeclaringClass();
        String className = declaringClass.getName();
        int separator = className.lastIndexOf('.');
        String packageName = separator < 0 ? "(default)" : className.substring(0, separator);
        List<String> parameterTypes = new ArrayList<>();
        for (Type type : method.getParameterTypes()) {
            parameterTypes.add(type.toString());
        }
        boolean application = declaringClass.isApplicationClass();
        return new CallGraphSnapshot.MethodNode(
                method.getSignature(), packageName, className, method.getName(),
                method.getSubSignature(), method.getReturnType().toString(), parameterTypes,
                Modifier.toString(method.getModifiers()), application, !application,
                method.isConstructor() || method.isStaticInitializer(),
                method.isConcrete(), depth, methodSourceLine(method));
    }

    private static int methodSourceLine(SootMethod method) {
        if (!method.isConcrete()) {
            return -1;
        }
        try {
            for (Unit unit : method.retrieveActiveBody().getUnits()) {
                int line = unit.getJavaSourceStartLineNumber();
                if (line > 0) {
                    return line;
                }
            }
        } catch (RuntimeException ignored) {
            // Source line evidence is optional; bytecode-only inputs often lack it.
        }
        return -1;
    }

    private static int sourceLine(Edge edge) {
        return edge.srcUnit() == null ? -1 : edge.srcUnit().getJavaSourceStartLineNumber();
    }

    private static void applyFanMetrics(
            Map<String, CallGraphSnapshot.MethodNode> nodes,
            List<CallGraphSnapshot.CallEdge> edges) {
        Map<String, Set<String>> incoming = new HashMap<>();
        Map<String, Set<String>> outgoing = new HashMap<>();
        for (CallGraphSnapshot.CallEdge edge : edges) {
            outgoing.computeIfAbsent(edge.getSource(), ignored -> new HashSet<>())
                    .add(edge.getTarget());
            incoming.computeIfAbsent(edge.getTarget(), ignored -> new HashSet<>())
                    .add(edge.getSource());
        }
        for (CallGraphSnapshot.MethodNode node : nodes.values()) {
            node.setFanIn(incoming.containsKey(node.getId())
                    ? incoming.get(node.getId()).size() : 0);
            node.setFanOut(outgoing.containsKey(node.getId())
                    ? outgoing.get(node.getId()).size() : 0);
        }
    }

    private static List<String> findUnreachableApplicationMethods(
            Set<String> reachable, CallGraphAnalysisOptions options) {
        List<String> result = new ArrayList<>();
        for (SootClass sootClass : Scene.v().getApplicationClasses()) {
            for (SootMethod method : sootClass.getMethods()) {
                if (!method.isConcrete()) {
                    continue;
                }
                if (!options.isIncludeConstructors()
                        && (method.isConstructor() || method.isStaticInitializer())) {
                    continue;
                }
                if (!reachable.contains(method.getSignature())) {
                    result.add(method.getSignature());
                }
            }
        }
        Collections.sort(result);
        return result;
    }

    private static List<String> buildWarnings(boolean truncated) {
        List<String> warnings = new ArrayList<>();
        warnings.add("Static call graphs may over-approximate runtime dispatch.");
        if (Scene.v().getPhantomClasses().size() > 0) {
            warnings.add(Scene.v().getPhantomClasses().size()
                    + " phantom classes were used because bytecode was unavailable.");
        }
        if (truncated) {
            warnings.add("The graph hit maxDepth or maxNodes; omitted calls may exist.");
        }
        return warnings;
    }

    private static List<List<String>> findRecursiveComponents(
            Set<String> nodeIds, List<CallGraphSnapshot.CallEdge> edges) {
        Map<String, List<String>> adjacency = new LinkedHashMap<>();
        for (String nodeId : nodeIds) {
            adjacency.put(nodeId, new ArrayList<>());
        }
        for (CallGraphSnapshot.CallEdge edge : edges) {
            List<String> targets = adjacency.get(edge.getSource());
            if (targets != null && adjacency.containsKey(edge.getTarget())
                    && !targets.contains(edge.getTarget())) {
                targets.add(edge.getTarget());
            }
        }
        for (List<String> targets : adjacency.values()) {
            Collections.sort(targets);
        }

        Tarjan tarjan = new Tarjan(adjacency);
        List<List<String>> result = new ArrayList<>();
        for (List<String> component : tarjan.components()) {
            if (component.size() > 1
                    || (component.size() == 1
                    && adjacency.get(component.get(0)).contains(component.get(0)))) {
                Collections.sort(component);
                result.add(component);
            }
        }
        result.sort(Comparator.comparing(component -> component.get(0)));
        return result;
    }

    private static final class Tarjan {
        private final Map<String, List<String>> adjacency;
        private final Map<String, Integer> indices = new HashMap<>();
        private final Map<String, Integer> lowLinks = new HashMap<>();
        private final Deque<String> stack = new ArrayDeque<>();
        private final Set<String> onStack = new HashSet<>();
        private final List<List<String>> components = new ArrayList<>();
        private int nextIndex;

        private Tarjan(Map<String, List<String>> adjacency) {
            this.adjacency = adjacency;
        }

        private List<List<String>> components() {
            for (String node : adjacency.keySet()) {
                if (!indices.containsKey(node)) {
                    visit(node);
                }
            }
            return components;
        }

        private void visit(String node) {
            indices.put(node, nextIndex);
            lowLinks.put(node, nextIndex);
            nextIndex++;
            stack.push(node);
            onStack.add(node);

            for (String target : adjacency.get(node)) {
                if (!indices.containsKey(target)) {
                    visit(target);
                    lowLinks.put(node, Math.min(lowLinks.get(node), lowLinks.get(target)));
                } else if (onStack.contains(target)) {
                    lowLinks.put(node, Math.min(lowLinks.get(node), indices.get(target)));
                }
            }

            if (lowLinks.get(node).equals(indices.get(node))) {
                List<String> component = new ArrayList<>();
                String current;
                do {
                    current = stack.pop();
                    onStack.remove(current);
                    component.add(current);
                } while (!node.equals(current));
                components.add(component);
            }
        }
    }
}
