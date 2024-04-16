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

    /**
     * Default constructor, initializes Backward Reachability using ICFG
     * @param appMainClass The main class of the application to be analyzed,Due to the ICFG based on whole program analysis,
     *                     it should be the entry point of the application or a class that contains the main method.
     */
    public BackwardReachabilityICFG(String appMainClass) {
        SootConfig sootConfig = new SootConfig();
        sootConfig.setupSoot(appMainClass, true);
        BiDiInterproceduralCFG<Unit, SootMethod> biDiInterproceduralCFG = new JimpleBasedInterproceduralCFG();
        this.icfg = new BackwardsInterproceduralCFG(biDiInterproceduralCFG);
    }

    /**
     * Constructor with class path
     * @param appMainClass The main class of the application to be analyzed,the ICFG based on whole program analysis,
     *                     it should be the entry point of the application or a class that contains the main method.
     *
     * @param classPath   The class path of the application to be analyzed it should be the path of the target class files
     */
    public BackwardReachabilityICFG(String appMainClass,String classPath) {
        SootConfig sootConfig = new SootConfig();
        sootConfig.setupSoot(appMainClass, true,classPath);
        BiDiInterproceduralCFG<Unit, SootMethod> biDiInterproceduralCFG = new JimpleBasedInterproceduralCFG();
        this.icfg = new BackwardsInterproceduralCFG(biDiInterproceduralCFG);
    }


    public List<Context> inDynamicExtent(SootMethod source, SootMethod target) {
        for (Unit start : icfg.getStartPointsOf(target)) {
            Context startingContext = new Context(start);
//            Context reached = reachable(startingContext, source);
//            if (reached != null) {
//                return reached;
//            }
            List<Context> reached = reachable(startingContext,source);
            if(!reached.isEmpty()){
                return reached;
            }
        }
        return null;
    }


    public List<Context> findUnknownSource(Context target)
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
    public List<Context> reachable(Context target, SootMethod source) {

        Deque<Context> worklist = new LinkedList<>();
        Set<Context> visited = new HashSet<>();
        List<Context> paths = new ArrayList<>();
        //初始化worklist，将target method的全部节点加入到worklist中
        Context lastNode = getMethodBodyContext(target, target.getReachedNode(), target.getReachedMethod());
        worklist.add(lastNode);
        visited.add(lastNode);

        while (!worklist.isEmpty()) {
            Context current = worklist.poll();
            Unit reachedNode = current.getReachedNode();
            SootMethod reachedMethod = icfg.getMethodOf(reachedNode);
            System.out.println("now reachedNode is: " + reachedNode + " in method: " + reachedMethod);
            
            if (reachedMethod.equals(source)) {
                Context down = getMethodBodyContext(current, reachedNode, reachedMethod);
                System.out.println("Find a path to the source method: " + down);
                paths.add(down);
                continue;
            }

            if (!icfg.getCallersOf(reachedMethod).isEmpty()) {
                for (Unit caller : icfg.getCallersOf(reachedMethod)) {
                    Context down = current.copy();
                    down.setReachedNode(caller);
                    down.setReachedMethod(reachedMethod);
                    down.getCallStack().addFirst(reachedNode);
                    down.getMethodCallStack().addFirst(reachedMethod);
                    Context up = getMethodBodyContext(down, caller, reachedMethod);
                    if (visited.add(up)) {
                        worklist.add(up);
                    }
                }
            }
        }
        return paths;
    }

    private Context getMethodBodyContext(Context current, Unit reachedNode, SootMethod reachedMethod) {
        Context currentContext = current.copy();
        Unit currentUnit = reachedNode;

        while (!icfg.getSuccsOf(currentUnit).isEmpty()) {
            for (Unit succ : icfg.getSuccsOf(currentUnit)) {
                Context down = currentContext.copy();
                down.setReachedNode(succ);
                down.setReachedMethod(reachedMethod);
                down.getCallStack().addFirst(currentUnit);
                down.getMethodCallStack().addFirst(reachedMethod);
                System.out.println("now reachedNode is: " + succ + " in method: " + reachedMethod);
                currentUnit = succ;
                currentContext = down;
            }
        }
        return currentContext;
    }

    public static void main(String[] args) {
        BackwardReachabilityICFG reachability = new BackwardReachabilityICFG("TestCaseDroid.test.Vulnerable","E:\\Tutorial\\TestCaseDroid\\target\\classes");
        SootMethod source = Scene.v().getSootClass("TestCaseDroid.test.Vulnerable").getMethodByName("main");
        SootMethod target = Scene.v().getSootClass("TestCaseDroid.test.ICFG").getMethodByName("test1");
        List<Context> reachedContext = reachability.inDynamicExtent(source, target);
        if (reachedContext != null && !reachedContext.isEmpty()) {
            System.out.println("The source method can be reached from the target method.");
            for (Context context : reachedContext) {
                System.out.println(context.getMethodCallStackString());
            }
        } else {
            System.out.println("The source method cannot be reached from the target method.");
        }
    }
}