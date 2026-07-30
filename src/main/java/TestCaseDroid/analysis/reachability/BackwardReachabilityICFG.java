package TestCaseDroid.analysis.reachability;

import TestCaseDroid.config.SootConfig;
import lombok.Setter;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Backward, method-sensitive reachability based on incoming call-graph edges.
 */
public class BackwardReachabilityICFG {
    private final CallGraph callGraph;
    @Setter
    private int maxDepth = 100;
    @Setter
    private int maxPaths = 100;

    public BackwardReachabilityICFG(String appMainClass) {
        new SootConfig().setupSoot(appMainClass, true);
        this.callGraph = Scene.v().getCallGraph();
    }

    public BackwardReachabilityICFG(String appMainClass, String classPath) {
        new SootConfig().setupSoot(appMainClass, true, classPath);
        this.callGraph = Scene.v().getCallGraph();
    }

    /**
     * Finds call chains proving that {@code source} can invoke {@code target}.
     * An empty list means no chain was found.
     */
    public List<Context> inDynamicExtent(SootMethod source, SootMethod target) {
        return search(target, source, false);
    }

    public List<Context> reachable(Context target, SootMethod source) {
        if (target == null || target.getReachedNode() == null) {
            throw new IllegalArgumentException("Target context must contain a reached node");
        }
        SootMethod targetMethod = target.getReachedMethod() != null
                ? target.getReachedMethod()
                : methodOf(target.getReachedNode());
        return search(targetMethod, source, false);
    }

    /**
     * Finds roots (main methods or methods without application callers) that can
     * reach the method containing {@code target}.
     */
    public List<Context> findUnknownSource(Context target) {
        if (target == null || target.getReachedNode() == null) {
            throw new IllegalArgumentException("Target context must contain a reached node");
        }
        SootMethod targetMethod = target.getReachedMethod() != null
                ? target.getReachedMethod()
                : methodOf(target.getReachedNode());
        return search(targetMethod, null, true);
    }

    private List<Context> search(SootMethod target, SootMethod expectedSource, boolean collectRoots) {
        validateBounds();
        Deque<BackwardPath> worklist = new ArrayDeque<>();
        worklist.add(new BackwardPath(target));
        List<Context> results = new ArrayList<>();

        while (!worklist.isEmpty() && results.size() < maxPaths) {
            BackwardPath current = worklist.removeFirst();
            SootMethod method = current.last();
            if (expectedSource != null && method.equals(expectedSource)) {
                results.add(current.toContext());
                continue;
            }
            if (current.edgeCount() >= maxDepth) {
                continue;
            }

            List<Edge> incoming = incomingApplicationEdges(method);
            if (collectRoots && (method.isMain() || incoming.isEmpty())) {
                results.add(current.toContext());
                continue;
            }
            for (Edge edge : incoming) {
                SootMethod caller = edge.src();
                if (!current.contains(caller)) {
                    worklist.addLast(current.append(caller, edge.srcUnit()));
                }
            }
        }
        return results;
    }

    private List<Edge> incomingApplicationEdges(SootMethod method) {
        List<Edge> result = new ArrayList<>();
        Iterator<Edge> incoming = callGraph.edgesInto(method);
        while (incoming.hasNext()) {
            Edge edge = incoming.next();
            if (!edge.src().isJavaLibraryMethod()) {
                result.add(edge);
            }
        }
        result.sort((left, right) -> left.src().getSignature()
                .compareTo(right.src().getSignature()));
        return result;
    }

    public void runAnalysis(MethodContext sourceMethod, MethodContext targetMethod) {
        SootMethod source = Scene.v().getMethod(sourceMethod.getMethodSignature());
        SootMethod target = Scene.v().getMethod(targetMethod.getMethodSignature());
        List<Context> paths = inDynamicExtent(source, target);
        if (paths.isEmpty()) {
            System.out.println("The source method cannot reach the target method.");
            return;
        }
        System.out.println("The source method can reach the target method.");
        for (Context context : paths) {
            System.out.println(context.getMethodCallStackString());
        }
    }

    private void validateBounds() {
        if (maxDepth < 0) {
            throw new IllegalStateException("maxDepth must be at least 0");
        }
        if (maxPaths < 1) {
            throw new IllegalStateException("maxPaths must be at least 1");
        }
    }

    private static SootMethod methodOf(Unit unit) {
        for (SootClass sootClass : Scene.v().getApplicationClasses()) {
            for (SootMethod method : sootClass.getMethods()) {
                if (method.isConcrete() && method.hasActiveBody()
                        && method.getActiveBody().getUnits().contains(unit)) {
                    return method;
                }
            }
        }
        throw new IllegalArgumentException("Reached node is not part of an application method: " + unit);
    }

    private static final class BackwardPath {
        /** target first, current caller last */
        private final List<SootMethod> methods;
        private final List<Unit> callSites;

        private BackwardPath(SootMethod target) {
            this.methods = Collections.singletonList(target);
            this.callSites = Collections.emptyList();
        }

        private BackwardPath(List<SootMethod> methods, List<Unit> callSites) {
            this.methods = methods;
            this.callSites = callSites;
        }

        private SootMethod last() {
            return methods.get(methods.size() - 1);
        }

        private int edgeCount() {
            return methods.size() - 1;
        }

        private boolean contains(SootMethod method) {
            return methods.contains(method);
        }

        private BackwardPath append(SootMethod caller, Unit callSite) {
            List<SootMethod> newMethods = new ArrayList<>(methods);
            newMethods.add(caller);
            List<Unit> newCallSites = new ArrayList<>(callSites);
            if (callSite != null) {
                newCallSites.add(callSite);
            }
            return new BackwardPath(newMethods, newCallSites);
        }

        private Context toContext() {
            List<SootMethod> sourceToTarget = new ArrayList<>(methods);
            Collections.reverse(sourceToTarget);
            Unit reachedNode = callSites.isEmpty() ? firstUnit(last()) : callSites.get(callSites.size() - 1);
            Context context = new Context(reachedNode, new LinkedList<>(callSites),
                    new LinkedList<>(sourceToTarget));
            context.setReachedMethod(last());
            context.setBackward(true);
            return context;
        }

        private static Unit firstUnit(SootMethod method) {
            return method.isConcrete() && method.hasActiveBody()
                    ? method.getActiveBody().getUnits().getFirst()
                    : null;
        }
    }
}
