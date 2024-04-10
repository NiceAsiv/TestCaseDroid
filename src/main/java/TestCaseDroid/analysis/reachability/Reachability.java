package TestCaseDroid.analysis.reachability;

import java.util.*;

import TestCaseDroid.config.SootConfig;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.InvokeStmt;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;

/**
 * The Reachability using ICFG
 */
public class Reachability {

    private final JimpleBasedInterproceduralCFG icfg;

    /**
     * Default constructor, initializes the interprocedural control flow graph (ICFG).
     */
    public Reachability(String targetClass) {
        SootConfig sootConfig = new SootConfig();
        sootConfig.setupSoot(targetClass, true);
        this.icfg = new JimpleBasedInterproceduralCFG();
    }

    public Context getExecutionPathFromEntryPoint(SootMethod targetMethod) {
        List<SootMethod> entryPoints = Scene.v().getEntryPoints();
        for(SootMethod entryPoint : entryPoints) {
            Context reachedContext = inDynamicExtent(entryPoint, targetMethod);
            if(reachedContext != null)
            {
                return reachedContext;
            }
        }
        return null;
    }

    /**
     * Determines if the target method can be reached from the source method in the dynamic extent of the source method.
     * @param source The source method
     * @param target The target method
     * @return The context of the reached target method if it can be reached, null otherwise
     */
    public Context inDynamicExtent(SootMethod source, SootMethod target) {
        for(Unit start : icfg.getStartPointsOf(source)) {
            Context startingContext = new Context(start);
            Context reached = reachable(startingContext, target);
            if(reached != null)
                return reached;
        }
        return null;
    }

    /**
     * Determines if the target method can be reached from the source context.
     * @param source The source context
     * @param target The target method
     * @return The context of the reached target method if it can be reached, null otherwise
     */
    public Context reachable(Context source, SootMethod target) {

        Deque<Context> worklist = new LinkedList<>();
        Set<Context> visited = new HashSet<>(); // Record the visited nodes
        worklist.add(source);
        visited.add(source);

        while(!worklist.isEmpty()) {
            Context current = worklist.poll(); // Get a node from the worklist
            Unit reachedNode = current.getReachedNode(); // Get the reached node
            SootMethod reachedMethod = icfg.getMethodOf(reachedNode);
            if(reachedMethod.equals(target))
            {
                System.out.println(current);
                return current;
            }

            //if the reached node is a call statement, add all the start points of the callee methods to the worklist
            if (reachedNode instanceof InvokeStmt) {
                final InvokeStmt invokeStmt = (InvokeStmt) reachedNode;
                final SootMethod targetMethod = invokeStmt.getInvokeExpr().getMethod();
                if (targetMethod.hasActiveBody()) {
                    final Unit targetStartPoint = targetMethod.getActiveBody().getUnits().getFirst();
                    Context targetContext = new Context(targetStartPoint);
                    if (!visited.contains(targetContext)) {
                        Context down = current.copy();
                        down.getCallStack().addFirst(reachedNode);
                        down.setReachedNode(targetStartPoint);
                        if(visited.add(down))
                        {
                            worklist.add(down);
                        }
                    }
                }
            }
            // If the reached node is not a call statement or an exit statement, add all its successors to the worklist
            List<Unit> succs = icfg.getSuccsOf(reachedNode);
            for(Unit succ : succs) {
                Context up = current.copy();
                up.setReachedNode(succ);
                up.getCallStack().addFirst(reachedNode);
                if(visited.add(up))
                {
                    worklist.add(up);
                }
            }
        }
        return null;
    }

    public static void main(String[] args) {
        Reachability reachability = new Reachability("TestCaseDroid.test.CallGraphs");
        SootMethod source = Scene.v().getSootClass("TestCaseDroid.test.CallGraphs").getMethod("void main(java.lang.String[])");
        SootMethod target = Scene.v().getSootClass("TestCaseDroid.test.A2").getMethod("void bar()");
        Context reachedContext = reachability.inDynamicExtent(source, target);
        if(reachedContext != null) {
            System.out.println("The target method can be reached from the source method.");
        } else {
            System.out.println("The target method cannot be reached from the source method.");
        }
    }
}