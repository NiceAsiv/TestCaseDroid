package TestCaseDroid.analysis.reachability;


import TestCaseDroid.config.SootConfig;
import lombok.Getter;
import lombok.Setter;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;

import java.util.*;


@Getter
@Setter
public class ReachabilityCG {
    private MethodContext entryMethodContext;
    private MethodContext targetMethodContext;
    private final CallGraph callGraph;

    /**
     * constructor of ReachabilityForFunction
     *
     * @param targetMethodSig the target method like "<TestCaseDroid.test.A2: void bar()>"
     * @param entryMethodSig  the entry method like "<TestCaseDroid.test.CallGraphs: void main(java.lang.String[])>"
     */
    public ReachabilityCG(String targetMethodSig, String entryMethodSig) {
        this.entryMethodContext = new MethodContext(entryMethodSig);
        this.targetMethodContext = new MethodContext(targetMethodSig);
        SootConfig sootConfig = new SootConfig();
        sootConfig.setupSoot(this.entryMethodContext.getClassName(), true);
        this.callGraph = Scene.v().getCallGraph();
    }

    public ReachabilityCG(String targetMethodSig, String entryMethodSig,String classPath) {
        this.entryMethodContext = new MethodContext(entryMethodSig);
        this.targetMethodContext = new MethodContext(targetMethodSig);
        SootConfig sootConfig = new SootConfig();
        sootConfig.setupSoot(this.entryMethodContext.getClassName(), true,classPath);
        this.callGraph = Scene.v().getCallGraph();
    }

    public MethodContext runAnalysis() {
        return analyzeCallGraph(this.entryMethodContext, this.targetMethodContext);
    }

    /**
     * analyze the call graph to find the call chain from the entry method to the target method
     * @return the call chain from the entry method to the target method
     *
     */
    public MethodContext analyzeCallGraph(MethodContext entryMethod, MethodContext targetMethod) {
        Set<String> visited = new HashSet<>();
        Queue<MethodContext> worklist = new LinkedList<>(); //这是一个队列，用于广度优先搜索
        worklist.offer(entryMethod);
        visited.add(entryMethod.getMethodSignature());

        while (!worklist.isEmpty()) {
            MethodContext current = worklist.poll();
            SootMethod currentMethod = Scene.v().getMethod(current.getMethodSignature());
//            System.out.println("currentMethod: " + currentMethod);
            if (currentMethod.getSignature().equals(targetMethod.getMethodSignature())) {
                MethodContext up = current.copy();
                up.getMethodCallStack().addFirst(currentMethod);
                return up;
            }
            Iterator<MethodOrMethodContext> targets = new Targets(callGraph.edgesOutOf(currentMethod));//获取所有被m调用的方法
            while (targets.hasNext()) {
                SootMethod target = (SootMethod) targets.next();
                if (!visited.contains(target.getSignature())&&!target.isJavaLibraryMethod()) {
                    MethodContext down = current.copy();
                    down.setMethodSignature(target.getSignature());
                    down.getMethodCallStack().addFirst(currentMethod);
                    visited.add(target.getSignature());
                    worklist.offer(down);
                }
            }
        }
        return null;
    }

    public static void main(String[] args) {
        ReachabilityCG analysis = new ReachabilityCG("<TestCaseDroid.test.ICFG: void test2()>", "<TestCaseDroid.test.Vulnerable: void main(java.lang.String[])>");
        MethodContext methodContext = analysis.runAnalysis();
        System.out.println(methodContext.getMethodCallStackString());
    }
}




