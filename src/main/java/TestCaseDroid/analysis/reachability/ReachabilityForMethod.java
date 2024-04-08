package TestCaseDroid.analysis.reachability;


import TestCaseDroid.graph.BuildCallGraph;
import TestCaseDroid.graph.BuildCallGraphForJar;
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
public class ReachabilityForMethod {
    private  MethodSig entryMethod;
    private  MethodSig targetMethod;

    /**
     * constructor of ReachabilityForFunction
     *
     * @param targetMethod the target method like "<TestCaseDroid.test.A2: void bar()>"
     * @param entryMethod  the entry method like "<TestCaseDroid.test.CallGraphs: void main(java.lang.String[])>"
     */
    public ReachabilityForMethod(String targetMethod, String entryMethod) {
        this.entryMethod = new MethodSig(entryMethod);
        this.targetMethod = new MethodSig(targetMethod);
    }

    /**
     * analyze the call graph to find the call chain from the entry method to the target method
     * @param callGraph the call graph
     * @return the call chain from the entry method to the target method
     *
     */
    private List<String> analyzeCallGraph(CallGraph callGraph) {
        List<String> callChain = new ArrayList<>(); //从入口函数到目标函数的调用链
        Set<String> visited = new HashSet<>();
        Map<SootMethod, SootMethod> predecessors = new HashMap<>(); // 记录前驱节点
        Queue<SootMethod> worklist = new LinkedList<>();

        //添加入口点到worklist
        for (SootMethod target : Scene.v().getEntryPoints()) {
            worklist.offer(target);
            visited.add(target.getSignature());
        }
        while (!worklist.isEmpty()) {
            SootMethod current = worklist.poll();//从worklist中取出一个方法
            visited.add(current.getSignature());

            if (current.getSignature().equals(this.targetMethod.signature)) {
                //找到目标函数，逆向回溯到入口函数
                SootMethod pred = current;
                while (!pred.getSignature().equals(this.entryMethod.signature)) {
                    callChain.add(pred.getSignature());
                    pred = predecessors.get(pred);
                }
                callChain.add(this.entryMethod.signature);
                break;
            }
            Iterator<MethodOrMethodContext> targets = new Targets(callGraph.edgesOutOf(current));//获取所有被m调用的方法
            while (targets.hasNext()) {
                SootMethod target = (SootMethod) targets.next();
                if (!visited.contains(target.getSignature())&&!target.isJavaLibraryMethod()) {
                    worklist.offer(target);
                    visited.add(target.getSignature());
                    predecessors.put(target, current);
                }
            }
        }
        Collections.reverse(callChain); // 反转列表，使得从入口函数开始
        String prettyCallChain = String.join(" -> ", callChain); // 使用箭头连接每个函数
        return Collections.singletonList(prettyCallChain); // 返回美化后的调用链
    }

    public List<String> runAnalysis(String targetJarPath) {
        BuildCallGraphForJar.buildCallGraphForJar(targetJarPath, entryMethod.className, entryMethod.methodName);
        CallGraph callGraph = Scene.v().getCallGraph();
        return analyzeCallGraph(callGraph);
    }
    public List<String> runAnalysis() {
        BuildCallGraph buildCallGraph = new BuildCallGraph(entryMethod.className, entryMethod.methodName);
        BuildCallGraph.buildCallGraphForClass();
        CallGraph callGraph = Scene.v().getCallGraph();
        return analyzeCallGraph(callGraph);
    }

    public static void main(String[] args) {
        ReachabilityForMethod analysis = new ReachabilityForMethod("<TestCaseDroid.test.A2: void bar()>", "<TestCaseDroid.test.CallGraphs: void main(java.lang.String[])>");
        List<String> callChain = analysis.runAnalysis("E:\\Tutorial\\TestCaseDroid\\target\\classes");
        System.out.println(callChain);
    }
}




