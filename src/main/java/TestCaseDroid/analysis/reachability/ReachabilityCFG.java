package TestCaseDroid.analysis.reachability;

import TestCaseDroid.config.SootConfig;
import soot.*;
import soot.jimple.InvokeStmt;
import soot.toolkits.graph.*;
import java.util.*;

public class ReachabilityCFG {
    private final ClassicCompleteUnitGraph cfg;

    public ReachabilityCFG(String targetClass,String srcMethodName) {
        SootConfig sootConfig = new SootConfig();
        sootConfig.setupSoot(targetClass, true);
        SootMethod srcMethod = Scene.v().getSootClass(targetClass).getMethodByName(srcMethodName);
        this.cfg = new ClassicCompleteUnitGraph(srcMethod.getActiveBody());
    }

    public Context inDynamicExtent(String targetInvokeMethod) {
        for (Unit start : cfg.getHeads()) {
            if (reachable(start, targetInvokeMethod) != null) {
                return reachable(start, targetInvokeMethod);
            }
        }
        return null;
    }

    public Context reachable(Unit source, String targetInvokeMethod) {
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
                if (targetMethod.getName().equals(targetInvokeMethod)) {
                    return currentContext;
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

    public static void main(String[] args) {

        ReachabilityCFG analysis = new ReachabilityCFG("TestCaseDroid.test.CFG", "method2");
        Context result = analysis.inDynamicExtent("method3");
        if (result != null) {
            System.out.println("The target method can be reached from the source method.");
            System.out.println("The path is: ");
            result.setBackward(true);
            System.out.println(result);
        } else {
            System.out.println("The target method cannot be reached from the source Node.");
        }
    }
}