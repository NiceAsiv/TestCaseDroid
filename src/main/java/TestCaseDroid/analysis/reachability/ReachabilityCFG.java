package TestCaseDroid.analysis.reachability;

import TestCaseDroid.config.SootConfig;
import TestCaseDroid.utils.DotGraphWrapper;
import soot.*;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.ReturnStmt;
import soot.toolkits.graph.*;
import soot.util.dot.DotGraphNode;

import java.util.*;

import static TestCaseDroid.utils.DotGraphWrapper.contextToDotGraph;

public class ReachabilityCFG {
    private MethodContext sourceMethodContext;
    private MethodContext targetMethodContext;
    private final CompleteUnitGraph cfg;

    public ReachabilityCFG(String entryClass, String targetMethodSig, String sourceMethodSig) {
        this.sourceMethodContext = new MethodContext(sourceMethodSig);
        this.targetMethodContext = new MethodContext(targetMethodSig);
        SootConfig sootConfig = new SootConfig();
        sootConfig.setupSoot(entryClass, true);
        SootMethod srcMethod = Scene.v().getMethod(sourceMethodContext.getMethodSignature());
        this.cfg = new CompleteUnitGraph(srcMethod.getActiveBody());
    }

    public ReachabilityCFG(String targetClass, String targetMethodSig, String sourceMethodSig, String classPath) {
        this.sourceMethodContext = new MethodContext(sourceMethodSig);
        this.targetMethodContext = new MethodContext(targetMethodSig);
        SootConfig sootConfig = new SootConfig();
        sootConfig.setupSoot(targetClass, true, classPath);
        SootMethod srcMethod = Scene.v().getMethod(sourceMethodContext.getMethodSignature());
        this.cfg = new CompleteUnitGraph(srcMethod.getActiveBody());
    }

    public ReachabilityCFG(String classNameForAnalysis, MethodContext sourceMethodContext,
            MethodContext targetMethodContext, String classPath) {
        this.sourceMethodContext = sourceMethodContext;
        this.targetMethodContext = targetMethodContext;
        SootConfig sootConfig = new SootConfig();
        sootConfig.setupSoot(classNameForAnalysis, true, classPath);
        SootMethod srcMethod = Scene.v().getMethod(sourceMethodContext.getMethodSignature());
        this.cfg = new CompleteUnitGraph(srcMethod.getActiveBody());
    }

    /**
     * 使用worklist算法进行分析 具体为：
     * 1. 从cfg的入口点开始，将入口点加入worklist 2. 从worklist中取出一个节点，将其所有的后继节点加入worklist 3.
     * 重复2，直到worklist为空
     */
    @Deprecated
    public void runAnalysis() {
        SootMethod targetMethod = Scene.v().getMethod(targetMethodContext.getMethodSignature());
        List<Context> paths = inDynamicExtent(targetMethod);
        if (paths.isEmpty()) {
            System.out.println("No path found from " + sourceMethodContext.getMethodSignature() + " to "
                    + targetMethodContext.getMethodSignature());
        } else {
            int pathIndex = 0;
            System.out.println("Found " + paths.size() + " paths from " + sourceMethodContext.getMethodSignature()
                    + " to " + targetMethodContext.getMethodSignature());
            for (Context path : paths) {
                path.setBackward(true);
                pathIndex++;
                System.out.println("The No." + pathIndex + " path:");
                System.out.println(path);
                contextToDotGraph(path, targetMethodContext.getClassName(), targetMethodContext.getMethodName(),
                        pathIndex);
            }
        }
    }

    public List<Context> inDynamicExtent(SootMethod targetInvokeMethod) {
        for (Unit start : cfg.getHeads()) {
            if (reachable(start, targetInvokeMethod) != null) {
                return reachable(start, targetInvokeMethod);
            }
        }
        return null;
    }

    public List<Context> reachable(Unit source, SootMethod targetInvokeMethod) {
        Deque<Unit> worklist = new LinkedList<>();
        HashSet<Unit> visited = new HashSet<>();
        List<Context> paths = new ArrayList<>();
        worklist.add(source);
        Context currentContext = new Context(source);
        while (!worklist.isEmpty()) {
            Unit current = worklist.poll();
            currentContext.setReachedNode(current);
            currentContext.getCallStack().push(current);
            System.out.println("now visiting: " + current);
            if (current instanceof InvokeStmt) {
                InvokeStmt invokeStmt = (InvokeStmt) current;
                SootMethod targetMethod = invokeStmt.getInvokeExpr().getMethod();
                if (targetMethod.equals(targetInvokeMethod)) {
                    paths.add(currentContext.copy());
                }
            } else if (current instanceof AssignStmt) {
                // check if the target method is invoked in the right-hand side of the
                // assignment
                AssignStmt assignStmt = (AssignStmt) current;
                if (assignStmt.containsInvokeExpr()) {
                    InvokeExpr invokeExpr = assignStmt.getInvokeExpr();
                    SootMethod targetMethod = invokeExpr.getMethod();
                    if (targetMethod.equals(targetInvokeMethod)) {
                        paths.add(currentContext.copy());
                    }
                }
            }
            for (Unit succ : cfg.getSuccsOf(current)) {
                if (visited.add(succ)) {
                    worklist.add(succ);
                }
            }
        }
        return paths;
    }

    /**
     * 直接标记可达节点，直接输出可达路径
     */
    public void runAnalysisUsingMarkNode() {
        SootMethod targetMethod = Scene.v().getMethod(targetMethodContext.getMethodSignature());
        List<DotGraphWrapper> dotPaths = markNodeForReachable();
        if (dotPaths.isEmpty()) {
            System.out.println("No path found from " + sourceMethodContext.getMethodSignature() + " to "
                    + targetMethodContext.getMethodSignature());
        } else {
            int pathIndex = 0;
            System.out.println("Found " + dotPaths.size() + " paths from " + sourceMethodContext.getMethodSignature()
                    + " to " + targetMethodContext.getMethodSignature());
            for (DotGraphWrapper dotPath : dotPaths) {
                pathIndex++;
                dotPath.plot(sourceMethodContext,targetMethodContext, pathIndex);
            }
        }
    }

    public List<DotGraphWrapper> markNodeForReachable() {
        DotGraphWrapper dotGraph = new DotGraphWrapper("cfg");
        int nodeId = 0;
        int pathIndex = 0;
        List<DotGraphWrapper> dotPaths = new ArrayList<>();
        Map<Unit, Integer> nodeIds = new HashMap<>();
        boolean hasTargetNode = false;
        for (Unit unit : cfg) {
            if (hasTargetNode) {
                System.out.println("Found a path to the target node in node: " + unit);
                // DotGraphWrapper currentPath = dotGraph.copy();
                pathIndex++;
                DotGraphWrapper thisDotGraphWrapper = dotGraph.copy();
                thisDotGraphWrapper.setGraphName(targetMethodContext.getMethodName() + "_path_" + pathIndex);
                darwLabel(cfg, thisDotGraphWrapper, nodeIds);
                dotPaths.add(thisDotGraphWrapper);
                hasTargetNode = false;
            }
            List<Unit> successors = cfg.getSuccsOf(unit);
            for (Unit successor : successors) {
                if (!nodeIds.containsKey(unit)) {
                    nodeIds.put(unit, nodeId++);
                }
                if (!nodeIds.containsKey(successor)) {
                    nodeIds.put(successor, nodeId++);
                }
                if (isTargetNode(successor, Scene.v().getMethod(targetMethodContext.getMethodSignature()))) {
                    hasTargetNode = true;
                }
                dotGraph.drawEdge(String.valueOf(nodeIds.get(unit)), String.valueOf(nodeIds.get(successor)));
            }
        }
        return dotPaths;
    }

    private void darwLabel(CompleteUnitGraph cfg, DotGraphWrapper dotGraph, Map<Unit, Integer> nodeIds) {
        for (Map.Entry<Unit, Integer> entry : nodeIds.entrySet()) {
            Unit unit = entry.getKey();
            Integer id = entry.getValue();
            DotGraphNode node = dotGraph.getNode(id.toString());
            node.setLabel(unit.toString());
            if (unit instanceof ReturnStmt) {
                node.setAttribute("style", "filled");
                node.setAttribute("fillcolor", "lightgray");
            } else if (unit.equals(cfg.getHeads().get(0))) {
                node.setAttribute("style", "filled");
                node.setAttribute("fillcolor", "gray");
            }
        }
    }

    // 判断是否为目标节点
    public Boolean isTargetNode(Unit unit, SootMethod targetInvokeMethod) {
        if (unit instanceof InvokeStmt) {
            InvokeStmt invokeStmt = (InvokeStmt) unit;
            SootMethod method = invokeStmt.getInvokeExpr().getMethod();
            return method.equals(targetInvokeMethod);
        } else if (unit instanceof AssignStmt) {
            AssignStmt assignStmt = (AssignStmt) unit;
            if (assignStmt.containsInvokeExpr()) {
                InvokeExpr invokeExpr = assignStmt.getInvokeExpr();
                SootMethod method = invokeExpr.getMethod();
                return method.equals(targetInvokeMethod);
            }
        }
        return false;
    }

    public static void main(String[] args) {
        ReachabilityCFG analysis = new ReachabilityCFG("TestCaseDroid.test.CFG",
                "<TestCaseDroid.test.CFG: void method3()>", "<TestCaseDroid.test.CFG: void method1(int,int)>");
        analysis.runAnalysisUsingMarkNode();
    }
}