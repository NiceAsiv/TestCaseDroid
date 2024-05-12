package TestCaseDroid.analysis.reachability;

import TestCaseDroid.config.SootConfig;
import soot.*;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.toolkits.graph.*;
import java.util.*;

import static TestCaseDroid.utils.DotGraphWrapper.contextToDotGraph;

public class ReachabilityCFG {
    private MethodContext sourceMethodContext;
    private MethodContext targetMethodContext;
    private final ClassicCompleteUnitGraph cfg;

    public ReachabilityCFG(String entryClass, String targetMethodSig, String sourceMethodSig) {
        this.sourceMethodContext = new MethodContext(sourceMethodSig);
        this.targetMethodContext = new MethodContext(targetMethodSig);
        SootConfig sootConfig = new SootConfig();
        sootConfig.setupSoot(entryClass, true);
        SootMethod srcMethod = Scene.v().getMethod(sourceMethodContext.getMethodSignature());
        this.cfg = new ClassicCompleteUnitGraph(srcMethod.getActiveBody());
    }

    public ReachabilityCFG(String targetClass, String targetMethodSig, String sourceMethodSig, String classPath) {
        this.sourceMethodContext = new MethodContext(sourceMethodSig);
        this.targetMethodContext = new MethodContext(targetMethodSig);
        SootConfig sootConfig = new SootConfig();
        sootConfig.setupSoot(targetClass, true, classPath);
        SootMethod srcMethod = Scene.v().getMethod(sourceMethodContext.getMethodSignature());
        this.cfg = new ClassicCompleteUnitGraph(srcMethod.getActiveBody());
    }

    public ReachabilityCFG(String classNameForAnalysis, MethodContext sourceMethodContext,
            MethodContext targetMethodContext, String classPath) {
        this.sourceMethodContext = sourceMethodContext;
        this.targetMethodContext = targetMethodContext;
        SootConfig sootConfig = new SootConfig();
        sootConfig.setupSoot(classNameForAnalysis, true, classPath);
        SootMethod srcMethod = Scene.v().getMethod(sourceMethodContext.getMethodSignature());
        this.cfg = new ClassicCompleteUnitGraph(srcMethod.getActiveBody());
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

    public static void main(String[] args) {
        ReachabilityCFG analysis = new ReachabilityCFG("TestCaseDroid.test.CFG",
                "<TestCaseDroid.test.CFG: void method3()>", "<TestCaseDroid.test.CFG: void method1(int,int)>");
        analysis.runAnalysis();
    }
}