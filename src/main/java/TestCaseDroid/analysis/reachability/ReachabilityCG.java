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
    private MethodContext sourceMethodContext;
    private MethodContext targetMethodContext;
    private final CallGraph callGraph;

    /**
     * constructor of ReachabilityForFunction
     * @param entryClass the entry class like "TestCaseDroid.test.CallGraphs"
     * @param targetMethodSig the target method like "<TestCaseDroid.test.A2: void bar()>"
     * @param sourceMethodSig  the entry method like "<TestCaseDroid.test.CallGraphs: void main(java.lang.String[])>"
     */
    public ReachabilityCG(String entryClass , String targetMethodSig, String sourceMethodSig) {
        this.sourceMethodContext = new MethodContext(sourceMethodSig);
        this.targetMethodContext = new MethodContext(targetMethodSig);
        SootConfig sootConfig = new SootConfig();
        sootConfig.setupSoot(entryClass, true);
        this.callGraph = Scene.v().getCallGraph();
    }

    public ReachabilityCG(String entryClass , String targetMethodSig, String entryMethodSig,String classPath) {
        this.sourceMethodContext = new MethodContext(entryMethodSig);
        this.targetMethodContext = new MethodContext(targetMethodSig);
        SootConfig sootConfig = new SootConfig();
        sootConfig.setupSoot(entryClass,true,classPath);
        this.callGraph = Scene.v().getCallGraph();
    }

    public List<MethodContext> runAnalysis() {
        return analyzeCallGraph(this.sourceMethodContext, this.targetMethodContext);
    }

    /**
     * analyze the call graph to find the call chain from the entry method to the target method
     * @return the call chain from the entry method to the target method
     *
     */
    public List<MethodContext> analyzeCallGraph(MethodContext entryMethod, MethodContext targetMethod) {
        Set<String> visited = new HashSet<>();
        Queue<MethodContext> worklist = new LinkedList<>(); //这是一个队列，用于广度优先搜索
        List<MethodContext> paths = new ArrayList<>();
        worklist.offer(entryMethod);
        visited.add(entryMethod.getMethodSignature());

        while (!worklist.isEmpty()) {
            MethodContext current = worklist.poll();
            SootMethod currentMethod = Scene.v().getMethod(current.getMethodSignature());
            if (currentMethod.getSignature().equals(targetMethod.getMethodSignature())) {
                MethodContext up = current.copy();
                up.getMethodCallStack().addFirst(currentMethod);
                paths.add(up);
                continue;
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
        return paths;
    }

    public static void main(String[] args) {
        ReachabilityCG analysis = new ReachabilityCG("TestCaseDroid.test.Vulnerable","<TestCaseDroid.test.ICFG: void test2()>", "<TestCaseDroid.test.Vulnerable: void main(java.lang.String[])>");
        List<MethodContext> methodContexts = analysis.runAnalysis();
        for (MethodContext methodContext : methodContexts) {
            System.out.println(methodContext.getMethodCallStackString());
        }
    }
}




