package TestCaseDroid.analysis.reachability;

import TestCaseDroid.config.SootConfig;
import TestCaseDroid.utils.SootUtils;
import lombok.Getter;
import lombok.Setter;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static TestCaseDroid.utils.DotGraphWrapper.methodContextToDotGraph;

/**
 * Enumerates bounded, cycle-free method paths in a Soot call graph.
 */
@Getter
@Setter
public class ReachabilityCG {
    private MethodContext sourceMethodContext;
    private MethodContext targetMethodContext;
    private final CallGraph callGraph;
    /** Maximum number of call edges in one returned path. */
    private int maxDepth = 100;
    /** Safety bound for graphs with a large number of alternative paths. */
    private int maxPaths = 1000;
    private boolean includeLibraryMethods;
    private boolean includeConstructors;

    public ReachabilityCG(String entryClass, String targetMethodSig, String sourceMethodSig) {
        this(entryClass, targetMethodSig, sourceMethodSig, null);
    }

    public ReachabilityCG(String entryClass, String targetMethodSig, String sourceMethodSig, String classPath) {
        this(entryClass, targetMethodSig, sourceMethodSig, classPath, null);
    }

    public ReachabilityCG(String entryClass, String targetMethodSig, String sourceMethodSig,
                          String classPath, String callGraphAlgorithm) {
        this.sourceMethodContext = new MethodContext(sourceMethodSig);
        this.targetMethodContext = new MethodContext(targetMethodSig);
        SootConfig sootConfig = new SootConfig();
        if (callGraphAlgorithm != null) {
            sootConfig.setCallGraphAlgorithm(callGraphAlgorithm);
        }
        sootConfig.setupSoot(entryClass, true, classPath,
                Collections.singletonList(sourceMethodContext.getMethodSignature()));
        this.callGraph = Scene.v().getCallGraph();
    }

    public ReachabilityCG(String classNameForAnalysis, MethodContext sourceMethodContext,
                          MethodContext targetMethodContext, String classPath) {
        this(classNameForAnalysis, sourceMethodContext, targetMethodContext, classPath, null);
    }

    public ReachabilityCG(String classNameForAnalysis, MethodContext sourceMethodContext,
                          MethodContext targetMethodContext, String classPath,
                          String callGraphAlgorithm) {
        this.sourceMethodContext = sourceMethodContext;
        this.targetMethodContext = targetMethodContext;
        SootConfig sootConfig = new SootConfig();
        if (callGraphAlgorithm != null) {
            sootConfig.setCallGraphAlgorithm(callGraphAlgorithm);
        }
        sootConfig.setupSoot(classNameForAnalysis, true, classPath,
                Collections.singletonList(sourceMethodContext.getMethodSignature()));
        this.callGraph = Scene.v().getCallGraph();
    }

    /** Constructor for callers that already own a configured Soot call graph. */
    public ReachabilityCG(CallGraph callGraph, MethodContext sourceMethodContext,
                          MethodContext targetMethodContext) {
        if (callGraph == null) {
            throw new IllegalArgumentException("Call graph must not be null");
        }
        this.callGraph = callGraph;
        this.sourceMethodContext = sourceMethodContext;
        this.targetMethodContext = targetMethodContext;
    }

    public void runAnalysis() {
        List<MethodContext> paths = analyzeCallGraph(sourceMethodContext, targetMethodContext);
        if (paths.isEmpty()) {
            System.out.println("No path found from " + sourceMethodContext.getMethodSignature()
                    + " to " + targetMethodContext.getMethodSignature());
            return;
        }

        System.out.println("Found " + paths.size() + " paths from "
                + sourceMethodContext.getMethodSignature() + " to "
                + targetMethodContext.getMethodSignature());
        int pathIndex = 0;
        for (MethodContext path : paths) {
            pathIndex++;
            System.out.println("Path " + pathIndex + ":");
            System.out.println(path.getMethodCallStackString());
            methodContextToDotGraph(path, sourceMethodContext, targetMethodContext, pathIndex);
        }
    }

    /**
     * Finds all simple paths up to {@link #maxDepth}. A method is allowed to
     * occur only once in a path, which terminates recursive/cyclic call graphs
     * without globally suppressing valid diamond-shaped alternatives.
     */
    public List<MethodContext> analyzeCallGraph(MethodContext source, MethodContext target) {
        validateBounds();
        SootMethod sourceMethod = Scene.v().getMethod(source.getMethodSignature());
        String targetSignature = target.getMethodSignature();

        Deque<PathState> worklist = new ArrayDeque<>();
        worklist.add(new PathState(sourceMethod));
        List<MethodContext> results = new ArrayList<>();

        while (!worklist.isEmpty() && results.size() < maxPaths) {
            PathState current = worklist.removeFirst();
            SootMethod currentMethod = current.last();
            if (currentMethod.getSignature().equals(targetSignature)) {
                results.add(toMethodContext(current.path));
                continue;
            }
            if (current.edgeCount() >= maxDepth) {
                continue;
            }

            for (SootMethod callee : calleesOf(currentMethod)) {
                if (!callee.getSignature().equals(targetSignature) && !isEligible(callee)) {
                    continue;
                }
                if (!current.contains(callee)) {
                    worklist.addLast(current.append(callee));
                }
            }
        }
        return results;
    }

    public boolean isReachable() {
        return !analyzeCallGraph(sourceMethodContext, targetMethodContext).isEmpty();
    }

    private List<SootMethod> calleesOf(SootMethod method) {
        Map<String, SootMethod> unique = new LinkedHashMap<>();
        Iterator<Edge> edges = callGraph.edgesOutOf(method);
        while (edges.hasNext()) {
            SootMethod target = edges.next().tgt();
            unique.put(target.getSignature(), target);
        }
        List<SootMethod> callees = new ArrayList<>(unique.values());
        callees.sort(Comparator.comparing(SootMethod::getSignature));
        return callees;
    }

    private boolean isEligible(SootMethod method) {
        if (!includeConstructors
                && ("<init>".equals(method.getName()) || "<clinit>".equals(method.getName()))) {
            return false;
        }
        return includeLibraryMethods
                || (!method.isJavaLibraryMethod() && SootUtils.isNotExcludedMethod(method));
    }

    private MethodContext toMethodContext(List<SootMethod> path) {
        Deque<SootMethod> reverseStack = new ArrayDeque<>();
        for (SootMethod method : path) {
            reverseStack.addFirst(method);
        }
        MethodContext result = new MethodContext(path.get(path.size() - 1).getSignature(), reverseStack);
        result.setIsBackwardReachability(true);
        return result;
    }

    private void validateBounds() {
        if (maxDepth < 0) {
            throw new IllegalStateException("maxDepth must be at least 0");
        }
        if (maxPaths < 1) {
            throw new IllegalStateException("maxPaths must be at least 1");
        }
    }

    private static final class PathState {
        private final List<SootMethod> path;

        private PathState(SootMethod source) {
            this.path = Collections.singletonList(source);
        }

        private PathState(List<SootMethod> path) {
            this.path = path;
        }

        private SootMethod last() {
            return path.get(path.size() - 1);
        }

        private int edgeCount() {
            return path.size() - 1;
        }

        private boolean contains(SootMethod method) {
            return path.contains(method);
        }

        private PathState append(SootMethod method) {
            List<SootMethod> copy = new ArrayList<>(path);
            copy.add(method);
            return new PathState(copy);
        }
    }
}
