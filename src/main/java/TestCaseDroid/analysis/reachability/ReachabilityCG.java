package TestCaseDroid.analysis.reachability;


import TestCaseDroid.config.SootConfig;
import TestCaseDroid.utils.SootUtils;
import lombok.Getter;
import lombok.Setter;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;

import java.util.*;

import static TestCaseDroid.utils.DotGraphWrapper.methodContextToDotGraph;


@Getter
@Setter
public class ReachabilityCG {
    private MethodContext sourceMethodContext;
    private MethodContext targetMethodContext;
    private final CallGraph callGraph;
    private int maxDepth = 999;

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

    public ReachabilityCG(String classNameForAnalysis, MethodContext sourceMethodContext, MethodContext targetMethodContext, String classPath) {
        this.sourceMethodContext = sourceMethodContext;
        this.targetMethodContext = targetMethodContext;
        SootConfig sootConfig = new SootConfig();
        sootConfig.setupSoot(classNameForAnalysis, true, classPath);
        this.callGraph = Scene.v().getCallGraph();
    }

    public void runAnalysis() {
        List<MethodContext> paths = analyzeCallGraph(this.sourceMethodContext, this.targetMethodContext);
        if (paths.isEmpty()) {
            System.out.println("No path found from " + this.sourceMethodContext.getMethodSignature() + " to " + this.targetMethodContext.getMethodSignature());
        } else {
            int pathIndex = 0;
            System.out.println("Found " + paths.size() + " paths from " + this.sourceMethodContext.getMethodSignature() + " to " + this.targetMethodContext.getMethodSignature());
            for (MethodContext path : paths) {
                pathIndex++;
                //path index
                System.out.println("The No." + pathIndex + " path:");
                System.out.println(path.getMethodCallStackString());
                methodContextToDotGraph(path,sourceMethodContext, targetMethodContext,pathIndex);
            }
        }
    }

    /**
     * analyze the call graph to find the call chain from the entry method to the target method
     * @return the call chain from the entry method to the target method
     *
     */
    public List<MethodContext> analyzeCallGraph(MethodContext entryMethod, MethodContext targetMethod) {
        Set<MethodContext> visited = new HashSet<>();
        Queue<MethodContext> worklist = new LinkedList<>(); //这是一个队列，用于广度优先搜索
        List<MethodContext> paths = new ArrayList<>();
        int depth = 0;
        worklist.offer(entryMethod);
        visited.add(entryMethod);
        while (!worklist.isEmpty()) {
            MethodContext current = worklist.poll();
            depth++;
            SootMethod currentMethod = Scene.v().getMethod(current.getMethodSignature());
//            System.out.println("Current method: " + currentMethod.getSignature() + "\npast call stack: " + current.getMethodCallStackString());
            if (currentMethod.getSignature().equals(targetMethod.getMethodSignature())) {
                MethodContext up = current.copy();
                up.getMethodCallStack().addFirst(currentMethod);
//                System.out.println("Found path: " + up.getMethodCallStackString());
                paths.add(up);
                continue;
            }

            if (depth >= maxDepth) {
                return paths;
            }

            Iterator<MethodOrMethodContext> targets = new Targets(callGraph.edgesOutOf(currentMethod));//获取所有被m调用的方法
            while (targets.hasNext()) {
                SootMethod target = (SootMethod) targets.next();
                if (!target.isJavaLibraryMethod()&& SootUtils.isNotExcludedMethod(target) && !target.getName().equals("<init>") && !target.getName().equals("<clinit>")) {
                    MethodContext down = current.copy();
                    down.setMethodSignature(target.getSignature());
                    down.getMethodCallStack().addFirst(currentMethod);
                    if (!visited.contains(down)) {
                        worklist.offer(down);
                        visited.add(down);
                    }
                }
            }
        }
        return paths;
    }

    public static void main(String[] args) {
        ReachabilityCG analysis = new ReachabilityCG("TestCaseDroid.test.Vulnerable","<TestCaseDroid.test.ICFG: void test2()>", "<TestCaseDroid.test.Vulnerable: void main(java.lang.String[])>");
        analysis.runAnalysis();
    }
}




