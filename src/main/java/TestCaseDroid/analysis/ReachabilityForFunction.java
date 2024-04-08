package TestCaseDroid.analysis;


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


/**
 * 使用worklist算法，并在目标函数开始分析，分析函数调用关系，并在逆向达到入口函数时停止分析。
 * <p>这个类使用了worklist算法来分析函数调用关系。worklist算法是一种广度优先搜索算法，它使用一个工作列表来存储待处理的函数。
 * 我们从目标函数开始，逆向寻找到入口函数的路径。我们使用一个栈来存储调用链，因为栈是后进先出的数据结构，这样可以保证我们的路径是逆向的。
 * 我们还使用了一个集合来存储已经访问过的函数，以避免重复处理。</p>
 */
@Getter
@Setter
public class ReachabilityForFunction {
    private  MethodSig entryMethod;
    private  MethodSig targetMethod;

    /**
     * 构造函数
     *
     * @param targetMethod 目标函数 like "<TestCaseDroid.test.A2: void bar()>"
     * @param entryMethod  入口函数 like "<TestCaseDroid.test.CallGraphs: void main(java.lang.String[])>"
     */
    public ReachabilityForFunction(String targetMethod, String entryMethod) {
        this.entryMethod = new MethodSig(entryMethod);
        this.targetMethod = new MethodSig(targetMethod);
    }

    /**
     * 分析函数调用图，并返回从入口函数到目标函数
     *
     * <p>这个方法接收一个CallGraph对象作为参数，然后使用worklist算法来分析函数调用关系。函数返回一个包含从入口函数到目标函数的调用链的列表。</p>
     *
     * @param callGraph 函数调用图
     * @return 从入口函数到目标函数的调用链
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

    /**
     * 已知目标函数，分析反向调用图 利用BackwardsInterproceduralCFG
     * @param targetMethod 目标函数
     * @return  从入口函数到目标函数的调用链
     */
    private List<String> analyzeBackICFG(String targetMethod) {
        List<String> callChain = new ArrayList<>(); //从目标函数到所有可能的入口函数的调用链
        Set<String> visited = new HashSet<>();
        Queue<SootMethod> worklist = new LinkedList<>();

        //添加目标函数到worklist
        SootMethod target = Scene.v().getMethod(targetMethod);
        worklist.offer(target);
        visited.add(target.getSignature());

        //创建一个BackwardsInterproceduralCFG的实例
//        BackwardsInterproceduralCFG bicfg = new BackwardsInterproceduralCFG();

        while (!worklist.isEmpty()) {
            SootMethod current = worklist.poll();//从worklist中取出一个方法
            visited.add(current.getSignature());

            callChain.add(current.getSignature());

        }

        return callChain;
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
        ReachabilityForFunction analysis = new ReachabilityForFunction("<TestCaseDroid.test.A2: void bar()>", "<TestCaseDroid.test.CallGraphs: void main(java.lang.String[])>");
        List<String> callChain = analysis.runAnalysis("E:\\Tutorial\\TestCaseDroid\\target\\classes");
        System.out.println(callChain);
    }
}




