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
    private final ClassicCompleteUnitGraph cfg;

    public ReachabilityCFG(String targetClass,String srcMethodName) {
        SootConfig sootConfig = new SootConfig();
        sootConfig.setupSoot(targetClass, true);
        SootMethod srcMethod = Scene.v().getSootClass(targetClass).getMethodByName(srcMethodName);
        this.cfg = new ClassicCompleteUnitGraph(srcMethod.getActiveBody());
    }
    public ReachabilityCFG(String targetClass,String srcMethodName,String classPath) {
        SootConfig sootConfig = new SootConfig();
        sootConfig.setupSoot(targetClass, true,classPath);
        SootMethod srcMethod = Scene.v().getSootClass(targetClass).getMethodByName(srcMethodName);
        this.cfg = new ClassicCompleteUnitGraph(srcMethod.getActiveBody());
    }

    public Context inDynamicExtent(SootMethod targetInvokeMethod) {
        for (Unit start : cfg.getHeads()) {
            if (reachable(start, targetInvokeMethod) != null) {
                return reachable(start, targetInvokeMethod);
            }
        }
        return null;
    }

    public Context reachable(Unit source, SootMethod targetInvokeMethod) {
        Deque<Unit> worklist = new LinkedList<>();
        HashSet<Unit> visited = new HashSet<>();
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
                    return currentContext;
                }
            } else if (current instanceof AssignStmt) {
                // check if the target method is invoked in the right-hand side of the assignment
                AssignStmt assignStmt = (AssignStmt) current;
                if (assignStmt.containsInvokeExpr()) {
                    InvokeExpr invokeExpr = assignStmt.getInvokeExpr();
                    SootMethod targetMethod = invokeExpr.getMethod();
                    if (targetMethod.equals(targetInvokeMethod)) {
                        return currentContext;
                    }
                }
            }
            for (Unit succ : cfg.getSuccsOf(current)) {
                if (visited.add(succ)) {
                    worklist.add(succ);
                }
            }
        }
        return null;
    }


    public void runAnalysis(MethodContext targetMethodContext) {
        SootMethod targetMethod = Scene.v().getMethod(targetMethodContext.getMethodSignature());
        Context reachedContext = inDynamicExtent(targetMethod);
        if (reachedContext != null) {
            System.out.println("The target method can be reached from the source method.");
            System.out.println("The path is: ");
            reachedContext.setBackward(true);
            System.out.println(reachedContext);
            contextToDotGraph(reachedContext, targetMethod.getDeclaringClass().getName(), targetMethod.getName());
        } else {
            System.out.println("The target method cannot be reached from the source method.");
        }
    }

    public static void main(String[] args) {

        ReachabilityCFG analysis = new ReachabilityCFG("TestCaseDroid.test.CFG", "method2");
        Context result = analysis.inDynamicExtent(Scene.v().getSootClass("TestCaseDroid.test.CFG").getMethodByName("method3"));
        if (result != null) {
            System.out.println("The target method can be reached from the source method.");
            System.out.println("The path is: ");
            result.setBackward(true);
            System.out.println(result);
            contextToDotGraph(result, "TestCaseDroid.test.CFG", "method1");
        } else {
            System.out.println("The target method cannot be reached from the source Node.");
        }
    }
}