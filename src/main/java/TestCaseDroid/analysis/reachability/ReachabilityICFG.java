package TestCaseDroid.analysis.reachability;

import java.util.*;

import TestCaseDroid.config.SootConfig;
import lombok.Setter;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.AssignStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;

/**
 * The Reachability using ICFG
 */
public class ReachabilityICFG {

    private final JimpleBasedInterproceduralCFG icfg;
    @Setter
    private int maxDepth = 999;

    /**
     * Default constructor, initializes the interprocedural control flow graph
     * (ICFG).
     */
    public ReachabilityICFG(String targetClass) {
        SootConfig sootConfig = new SootConfig();
        sootConfig.setupSoot(targetClass, true);
        this.icfg = new JimpleBasedInterproceduralCFG();
    }
    public ReachabilityICFG(String targetClass,String classPath) {
        SootConfig sootConfig = new SootConfig();
        sootConfig.setupSoot(targetClass, true,classPath);
        this.icfg = new JimpleBasedInterproceduralCFG();
    }


    /**
     * Determines if the target method can be reached from the source method in the
     * dynamic extent of the source method.
     * 
     * @param source The source method
     * @param target The target method
     * @return The context of the reached target method if it can be reached, null
     *         otherwise
     */
    public List<Context> inDynamicExtent(SootMethod source, SootMethod target) {
        for (Unit start : icfg.getStartPointsOf(source)) {
            Context startingContext = new Context(start);
            List<Context> reached = reachable(startingContext, target);
             if(!reached.isEmpty()){
                 return reached;
             }
        }
        return null;
    }

    /**
     * This method is used to determine the reachability from a source method to a target method.
     * It uses a worklist algorithm to traverse the interprocedural control flow graph (ICFG).
     * The algorithm starts from the source method and adds it to the worklist.
     * It then iteratively removes an element from the worklist, checks if it is the target method, and if so, adds it to the paths.
     * If the element is not the target method, it checks if it is a function call statement.
     * If it is a function call statement, it adds all the nodes of the called function to the worklist.
     * The process continues until the worklist is empty or the maximum depth is reached.
     *
     * @param source The source context from which the reachability is to be determined.
     * @param target The target method to which the reachability is to be determined.
     * @return A list of contexts that represent the paths from the source to the target.
     */
    public List<Context> reachable(Context source, SootMethod target) {
        Deque<Context> worklist = new LinkedList<>();
        HashSet<Context> visited = new HashSet<>(); // Record the visited nodes
        List<Context> paths = new ArrayList<>();
        int depth = 0;
        SootMethod sourceMethod = icfg.getMethodOf(source.getReachedNode());

        //初始化worklist，将source method的全部节点加入到worklist中
        getInvokeMethodWorklist(source, sourceMethod, visited, worklist);

        //开始遍历worklist
        while (!worklist.isEmpty()) {
            Context current = worklist.poll(); // Get a node from the worklist
            Unit reachedNode = current.getReachedNode(); // Get the reached node
            SootMethod reachedMethod = icfg.getMethodOf(reachedNode);
            System.out.println("Searching .... now reached Node: " + reachedNode + " in method: " + reachedMethod);

            //匹配到目标方法
            if (reachedMethod.equals(target)) {
                System.out.println("Find a path to the target method: " + target);
                System.out.println(current);
                paths.add(current);
                continue;
            }
            if (depth >= maxDepth) {
                return paths;
            }

            //如果遇到函数调用语句，将被调用函数的的节点内容全部加入到worklist中
            if (reachedNode instanceof InvokeStmt) {
                final InvokeStmt invokeStmt = (InvokeStmt) reachedNode;
                final SootMethod targetMethod = invokeStmt.getInvokeExpr().getMethod();
                if (targetMethod.hasActiveBody()) {
                    depth++;
                    //遍历被调用函数的所有节点
                    getInvokeMethodWorklist(current, targetMethod, visited, worklist);
                }
            } else if (reachedMethod instanceof AssignStmt) {
                // check if the target method is invoked in the right-hand side of the assignment
                AssignStmt assignStmt = (AssignStmt) reachedNode;
                if (assignStmt.containsInvokeExpr()) {
                    SootMethod targetMethod = assignStmt.getInvokeExpr().getMethod();
                    if (targetMethod.hasActiveBody()){
                        depth++;
                        getInvokeMethodWorklist(current, targetMethod, visited, worklist);
                    }
                }
            }
        }
        return paths;
    }

    /**
     * This method is used to add all the nodes of a method to the worklist.
     * It starts from the current context and iterates over all the units of the method.
     * If a unit is not visited, it creates a new context for it and adds it to the worklist.
     * If the unit is an InvokeStmt and the method is not a constructor, it sets the invokeFlag to true.
     * If the method does not call any other methods, it adds the last node of the method to the worklist.
     *
     * @param current The current context from which the method is invoked.
     * @param Method The method whose nodes are to be added to the worklist.
     * @param visited A set of units that have been visited.
     * @param worklist The worklist to which the nodes are to be added.
     */
    private static void getInvokeMethodWorklist(Context current, SootMethod Method, HashSet<Context> visited, Deque<Context> worklist) {
        Context BeforeInvoke = current.copy();
        boolean invokeFlag = false;
        for (Unit targetStartPoint : Method.getActiveBody().getUnits()) {
            Context down = BeforeInvoke.copy();
            down.getCallStack().add(BeforeInvoke.getReachedNode());
            down.getMethodCallStack().add(Method);
            down.setReachedNode(targetStartPoint);
            down.setReachedMethod(Method);
            //如果是函数调用语句，还要遍历函数体,需要排除对象的构造函数 以及调用java库函数的语句
            if (visited.add(down) && targetStartPoint instanceof InvokeStmt && !targetStartPoint.toString().contains("<init>")
                    && !targetStartPoint.toString().contains("void <clinit>") && !((InvokeStmt) targetStartPoint).getInvokeExpr().getMethod().isJavaLibraryMethod()) {
                worklist.add(down);
                invokeFlag = true;
            }
            BeforeInvoke = down;
        }
        //如果被调用函数没有调用其他函数，则将函数的最后一个节点(已包含它之前的所有节点)加入到worklist中
        if (!invokeFlag) {
            worklist.add(BeforeInvoke);
        }
    }
    public void runAnalysis(MethodContext entryMethod, MethodContext targetMethod) {
        SootMethod source = Scene.v().getMethod(entryMethod.getMethodSignature());
        SootMethod target = Scene.v().getMethod(targetMethod.getMethodSignature());
        List<Context> reachedContext = inDynamicExtent(source, target);
        if (reachedContext != null && !reachedContext.isEmpty()) {
            System.out.println("The target method can be reached from the source method.");
            for (Context context : reachedContext) {
                System.out.println(context.getMethodCallStackString());
            }
        } else {
            System.out.println("The target method cannot be reached from the source method.");
        }
    }

    public static void main(String[] args) {
        ReachabilityICFG ReachabilityICFG = new ReachabilityICFG("TestCaseDroid.test.Vulnerable");
        SootMethod source = Scene.v().getSootClass("TestCaseDroid.test.Vulnerable")
                .getMethod("void main(java.lang.String[])");
        SootMethod target = Scene.v().getSootClass("TestCaseDroid.test.ICFG").getMethod("void test1()");
        List<Context> reachedContext = ReachabilityICFG.inDynamicExtent(source, target);
        if (reachedContext != null && !reachedContext.isEmpty()) {
            System.out.println("The target method can be reached from the source method.");
        } else {
            System.out.println("The target method cannot be reached from the source method.");
        }
    }
}