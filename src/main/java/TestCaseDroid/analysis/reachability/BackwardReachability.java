package TestCaseDroid.analysis.reachability;

import TestCaseDroid.config.SootConfig;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.ide.icfg.BackwardsInterproceduralCFG;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;

import java.util.*;

public class BackwardReachability {

    private final BackwardsInterproceduralCFG icfg;
    private String ClassName;
    private String MethodName;

    public BackwardReachability(String targetClass, String targetMethod) {
        this.ClassName = targetClass;
        this.MethodName = targetMethod;
        SootConfig sootConfig = new SootConfig();
        sootConfig.setupSoot(this.ClassName, true);
        BiDiInterproceduralCFG<Unit, SootMethod> biDiInterproceduralCFG = new JimpleBasedInterproceduralCFG();
        this.icfg = new BackwardsInterproceduralCFG(biDiInterproceduralCFG);
    }

    public Context inDynamicExtent(SootMethod source, SootMethod target) {
        for (Unit start : icfg.getStartPointsOf(target)) {
            Context startingContext = new Context(start);
            Context reached = reachable(startingContext, source);
            if (reached != null) {
                return reached;
            }
        }
        return null;
    }

    public Context reachable(Context source, SootMethod target) {

        Deque<Context> worklist = new LinkedList<>();
        Set<Context> visited = new HashSet<>();
        worklist.add(source);
        visited.add(source);

        while (!worklist.isEmpty()) {
            Context current = worklist.poll();
            Unit reachedNode = current.getReachedNode();
            SootMethod reachedMethod = icfg.getMethodOf(reachedNode);
            System.out.println("now reachedNode is: " + reachedNode + " in method: " + reachedMethod);
            if (reachedMethod.equals(target)) {
                for (Unit u : current.getCallStack()) {
                    System.out.println(u);
                }
                return current;
            }
            List<Unit> preds = icfg.getPredsOf(reachedNode);
            for (Unit pred : preds) {
                Context up = current.copy();
                up.setReachedNode(pred);
                up.getCallStack().addFirst(reachedNode);
                if (visited.add(up)) {
                    worklist.add(up);
                }
            }
            if (worklist.isEmpty()) {
                Collection<Unit> callers = icfg.getCallersOf(reachedMethod);
                for (Unit caller : callers) {
                    if (visited.stream().noneMatch(context -> context.getReachedNode().equals(caller))) {
                        Context up = current.copy();
                        up.setReachedNode(caller);
                        up.getCallStack().addFirst(reachedNode);
                        if (visited.add(up)) {
                            worklist.add(up);
                        }
                    }

                }
            }
        }
        return null;
    }

    public static void main(String[] args) {
        BackwardReachability reachability = new BackwardReachability("TestCaseDroid.test.CallGraphs", "main");
        SootMethod source = Scene.v().getMainMethod();
        SootMethod target = Scene.v().getSootClass("TestCaseDroid.test.A2").getMethod("void bar()");
        Context reachedContext = reachability.inDynamicExtent(source, target);
        if (reachedContext != null) {
            System.out.println("The source method can be reached from the target method.");
        } else {
            System.out.println("The source method cannot be reached from the target method.");
        }
    }
}