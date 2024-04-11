package TestCaseDroid.analysis.reachability;

import TestCaseDroid.config.SootConfig;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.ide.icfg.BackwardsInterproceduralCFG;
import soot.jimple.toolkits.ide.icfg.BiDiInterproceduralCFG;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;

import java.util.*;

public class BackwardReachabilityICFG {

    private final BackwardsInterproceduralCFG icfg;

    public BackwardReachabilityICFG(String targetClass, String targetMethod) {
        SootConfig sootConfig = new SootConfig();
        sootConfig.setupSoot(targetClass, true);
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


    public List<Context> reachable(Context target)
    {
        List<Context> paths = new ArrayList<>();
        Deque<Context> worklist = new LinkedList<>(); // Deque is a double-ended queue
        Set<Context> visited = new HashSet<>();
        worklist.add(target);
        visited.add(target);
        while (!worklist.isEmpty()) {
            Context current = worklist.poll();
            Unit reachedNode = current.getReachedNode();
            SootMethod reachedMethod = icfg.getMethodOf(reachedNode);
            System.out.println("now reachedNode is: " + reachedNode + " in method: " + reachedMethod);
            Collection<Unit> callers = icfg.getCallersOf(reachedMethod);
            if (reachedMethod.isMain()||callers.isEmpty()) {
                paths.add(current);
            }else {
                for (Unit caller : callers) {
                    Context up = current.copy();
                    up.setReachedNode(caller);
                    up.getCallStack().addFirst(reachedNode);
                    up.getMethodCallStack().addFirst(reachedMethod);
                    if (visited.add(up)) {
                        worklist.add(up);
                    }
                }
            }
        }
        return paths;
    }

    /**
     * search the source method from the target method
     * @param source The source context
     * @param target The target method
     * @return The context of the reached target method if it can be reached, null otherwise
     */
    public Context reachable(Context target, SootMethod source) {

        Deque<Context> worklist = new LinkedList<>();
        Set<Context> visited = new HashSet<>();
        worklist.add(target);
        visited.add(target);
        while (!worklist.isEmpty()) {
            Context current = worklist.poll();
            Unit reachedNode = current.getReachedNode();
            SootMethod reachedMethod = icfg.getMethodOf(reachedNode);
            System.out.println("now reachedNode is: " + reachedNode + " in method: " + reachedMethod);
            if (reachedMethod.equals(source)) {
                Context up = current.copy();
                up.getCallStack().addFirst(reachedNode);
                up.getMethodCallStack().addFirst(reachedMethod);
                Context down = up.copy();
                List<Unit> succs = icfg.getSuccsOf(reachedNode);
                for (Unit succ : succs) {
                    down.setReachedNode(succ);
                    down.getCallStack().addFirst(succ);
                    down.getMethodCallStack().addFirst(icfg.getMethodOf(succ));
                    System.out.println("now reachedNode is: " + succ + " in method: " + reachedMethod);
                }
                return down;
            }
            List<Unit> Succs = icfg.getSuccsOf(reachedNode);
            for (Unit succ : Succs) {
                Context down = current.copy();
                down.setReachedNode(succ);
                down.getCallStack().addFirst(reachedNode);
                down.getMethodCallStack().addFirst(reachedMethod);
                if (visited.add(down)) {
                    worklist.add(down);
                }
            }
            if (worklist.isEmpty()) {
                Collection<Unit> callers = icfg.getCallersOf(reachedMethod);
                for (Unit caller : callers) {
                    if (visited.stream().noneMatch(context -> context.getReachedNode().equals(caller))) {
                        Context up = current.copy();
                        up.setReachedNode(caller);
                        up.getCallStack().addFirst(reachedNode);
                        up.getMethodCallStack().addFirst(reachedMethod);
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
        BackwardReachabilityICFG reachability = new BackwardReachabilityICFG("TestCaseDroid.test.CallGraphs", "main");
        SootMethod source = Scene.v().getSootClass("TestCaseDroid.test.CallGraphs").getMethod("void doStuff()");
        SootMethod target = Scene.v().getSootClass("TestCaseDroid.test.A2").getMethod("void bar()");
        Context reachedContext = reachability.inDynamicExtent(source, target);
        if (reachedContext != null) {
            System.out.println("The source method can be reached from the target method.");
            System.out.println("The Call Stack is:\n " + reachedContext);
            System.out.println("The Method Call Stack is:\n " + reachedContext.getMethodCallStackString());
        } else {
            System.out.println("The source method cannot be reached from the target method.");
        }
    }
}