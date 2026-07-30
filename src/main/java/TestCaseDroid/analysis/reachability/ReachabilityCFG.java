package TestCaseDroid.analysis.reachability;

import TestCaseDroid.config.SootConfig;
import TestCaseDroid.utils.DotGraphWrapper;
import lombok.Setter;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.Stmt;
import soot.toolkits.graph.CompleteUnitGraph;
import soot.util.dot.DotGraphNode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static TestCaseDroid.utils.DotGraphWrapper.contextToDotGraph;

/**
 * Finds invocation sites reachable inside one method's control-flow graph.
 */
public class ReachabilityCFG {
    private final MethodContext sourceMethodContext;
    private final MethodContext targetMethodContext;
    private final CompleteUnitGraph cfg;
    @Setter
    private int maxDepth = 10_000;
    @Setter
    private int maxPaths = 1000;

    public ReachabilityCFG(String entryClass, String targetMethodSig, String sourceMethodSig) {
        this(entryClass, new MethodContext(sourceMethodSig),
                new MethodContext(targetMethodSig), null);
    }

    public ReachabilityCFG(String targetClass, String targetMethodSig,
                           String sourceMethodSig, String classPath) {
        this(targetClass, new MethodContext(sourceMethodSig),
                new MethodContext(targetMethodSig), classPath);
    }

    public ReachabilityCFG(String classNameForAnalysis, MethodContext sourceMethodContext,
                           MethodContext targetMethodContext, String classPath) {
        this.sourceMethodContext = sourceMethodContext;
        this.targetMethodContext = targetMethodContext;
        new SootConfig().setupSoot(classNameForAnalysis, false, classPath);
        SootMethod source = Scene.v().getMethod(sourceMethodContext.getMethodSignature());
        this.cfg = new CompleteUnitGraph(source.retrieveActiveBody());
    }

    public void runAnalysis() {
        List<Context> paths = inDynamicExtent(
                Scene.v().getMethod(targetMethodContext.getMethodSignature()));
        printSummary(paths);
        int pathIndex = 0;
        for (Context path : paths) {
            pathIndex++;
            System.out.println("Path " + pathIndex + ":");
            System.out.println(path);
            contextToDotGraph(path, targetMethodContext.getClassName(),
                    targetMethodContext.getMethodName(), pathIndex);
        }
    }

    public List<Context> inDynamicExtent(SootMethod targetInvokeMethod) {
        validateBounds();
        List<Context> paths = new ArrayList<>();
        for (Unit start : cfg.getHeads()) {
            paths.addAll(reachable(start, targetInvokeMethod));
            if (paths.size() >= maxPaths) {
                break;
            }
        }
        return paths;
    }

    /**
     * Enumerates simple CFG paths ending at an invocation of the target method.
     * Loop back-edges are not revisited within one path.
     */
    public List<Context> reachable(Unit source, SootMethod targetInvokeMethod) {
        if (source == null || targetInvokeMethod == null) {
            throw new IllegalArgumentException("Source unit and target method must not be null");
        }
        validateBounds();
        Deque<UnitPath> worklist = new ArrayDeque<>();
        worklist.add(new UnitPath(source));
        List<Context> paths = new ArrayList<>();

        while (!worklist.isEmpty() && paths.size() < maxPaths) {
            UnitPath current = worklist.removeFirst();
            Unit unit = current.last();
            if (isTargetNode(unit, targetInvokeMethod)) {
                Context context = new Context(unit, new LinkedList<>(current.units));
                context.setReachedMethod(targetInvokeMethod);
                paths.add(context);
                continue;
            }
            if (current.edgeCount() >= maxDepth) {
                continue;
            }
            for (Unit successor : cfg.getSuccsOf(unit)) {
                if (!current.contains(successor)) {
                    worklist.addLast(current.append(successor));
                }
            }
        }
        return paths;
    }

    public void runAnalysisUsingMarkNode() {
        List<DotGraphWrapper> dotPaths = markNodeForReachable();
        printSummaryCount(dotPaths.size());
        int pathIndex = 0;
        for (DotGraphWrapper dotPath : dotPaths) {
            dotPath.plot(sourceMethodContext, targetMethodContext, ++pathIndex);
        }
    }

    /** Builds one DOT graph per real witness path. */
    public List<DotGraphWrapper> markNodeForReachable() {
        SootMethod target = Scene.v().getMethod(targetMethodContext.getMethodSignature());
        List<DotGraphWrapper> result = new ArrayList<>();
        int pathIndex = 0;
        for (Context context : inDynamicExtent(target)) {
            DotGraphWrapper graph = new DotGraphWrapper(
                    targetMethodContext.getMethodName() + "_path_" + (++pathIndex));
            Map<Unit, String> ids = new IdentityHashMap<>();
            Unit previous = null;
            int id = 0;
            for (Unit unit : context.getCallStack()) {
                String nodeId = ids.get(unit);
                if (nodeId == null) {
                    nodeId = Integer.toString(id++);
                    ids.put(unit, nodeId);
                    graph.drawNode(nodeId);
                    DotGraphNode node = graph.getNode(nodeId);
                    node.setLabel(unit.toString());
                    if (unit.equals(context.getReachedNode())) {
                        node.setAttribute("style", "filled");
                        node.setAttribute("fillcolor", "lightgreen");
                    }
                }
                if (previous != null) {
                    graph.drawEdge(ids.get(previous), nodeId);
                }
                previous = unit;
            }
            result.add(graph);
        }
        return result;
    }

    public Boolean isTargetNode(Unit unit, SootMethod targetInvokeMethod) {
        if (!(unit instanceof Stmt)) {
            return false;
        }
        Stmt statement = (Stmt) unit;
        return statement.containsInvokeExpr()
                && statement.getInvokeExpr().getMethod().equals(targetInvokeMethod);
    }

    private void printSummary(List<Context> paths) {
        printSummaryCount(paths.size());
    }

    private void printSummaryCount(int count) {
        if (count == 0) {
            System.out.println("No path found from " + sourceMethodContext.getMethodSignature()
                    + " to " + targetMethodContext.getMethodSignature());
        } else {
            System.out.println("Found " + count + " paths from "
                    + sourceMethodContext.getMethodSignature() + " to "
                    + targetMethodContext.getMethodSignature());
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

    private static final class UnitPath {
        private final List<Unit> units;

        private UnitPath(Unit source) {
            this.units = Collections.singletonList(source);
        }

        private UnitPath(List<Unit> units) {
            this.units = units;
        }

        private Unit last() {
            return units.get(units.size() - 1);
        }

        private int edgeCount() {
            return units.size() - 1;
        }

        private boolean contains(Unit unit) {
            return units.contains(unit);
        }

        private UnitPath append(Unit unit) {
            List<Unit> copy = new ArrayList<>(units);
            copy.add(unit);
            return new UnitPath(copy);
        }
    }
}
