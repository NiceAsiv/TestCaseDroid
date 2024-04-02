package TestCaseDroid.analysis;


import TestCaseDroid.graph.BuildCallGraph;
import TestCaseDroid.graph.BuildCallGraphForJar;
import lombok.Getter;
import lombok.Setter;
import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;
import soot.jimple.toolkits.callgraph.ReachableMethods;
import soot.jimple.toolkits.callgraph.Targets;
import soot.util.queue.QueueReader;

import java.util.*;


/**
 * 使用worklist算法，并在目标函数开始分析，分析函数调用关系，并在逆向达到入口函数时停止分析。
 * <p>这个类使用了worklist算法来分析函数调用关系。worklist算法是一种广度优先搜索算法，它使用一个工作列表来存储待处理的函数。
 * 我们从目标函数开始，逆向寻找到入口函数的路径。我们使用一个栈来存储调用链，因为栈是后进先出的数据结构，这样可以保证我们的路径是逆向的。
 * 我们还使用了一个集合来存储已经访问过的函数，以避免重复处理。</p>
 */
@Getter
@Setter
public class ReachabilityAnalysis {
    private  String entryMethod;
    private  String targetMethod;

    public ReachabilityAnalysis(String targetMethod, String entryMethod) {
        this.entryMethod = entryMethod;
        this.targetMethod = targetMethod;
    }
    /**
     * 分析函数调用图，并返回从目标函数逆向到入口函数的调用链。
     *
     * <p>这个方法接收一个CallGraph对象作为参数，然后使用worklist算法来分析函数调用关系。函数返回一个包含从目标函数逆向到入口函数的调用链的列表。</p>
     *
     * @param callGraph 函数调用图
     * @return 从目标函数逆向到入口函数的调用链
     */
    public List<String> analyzeCallGraph(CallGraph callGraph) {
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

            if (current.getSignature().equals(this.targetMethod)) {
                //找到目标函数，逆向回溯到入口函数
                SootMethod pred = current;
                while (!pred.getSignature().equals(this.entryMethod)) {
                    callChain.add(pred.getSignature());
                    pred = predecessors.get(pred);
                }
                callChain.add(this.entryMethod);
                break;
            }
            Iterator<MethodOrMethodContext> targets = new Targets(callGraph.edgesOutOf(current));//获取所有被m调用的方法
            while (targets.hasNext()) {
                SootMethod target = (SootMethod) targets.next();
                if (!visited.contains(target.getSignature())) {
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
    private static void getReachabilityAnalysis(CallGraph callGraph) {
        // Create a ReachableMethods object
        ReachableMethods reachableMethods = new ReachableMethods(callGraph, Scene.v().getEntryPoints().iterator());

        // Update the ReachableMethods object to get all reachable methods
        reachableMethods.update();

        // Create a QueueReader object to read all reachable methods
        QueueReader<MethodOrMethodContext> reader = reachableMethods.listener();

        // Iterate and print all reachable methods
        while (reader.hasNext()) {
            MethodOrMethodContext edge = reader.next();
            System.out.println("Reachable method: " + edge.method().getSignature());
        }
    }

    public static void main(String[] args) {
        ReachabilityAnalysis analysis = new ReachabilityAnalysis("<TestCaseDroid.test.A2: void bar()>", "<TestCaseDroid.test.CallGraphs: void main(java.lang.String[])>");
        BuildCallGraphForJar.buildCallGraphForJar("E:\\Tutorial\\TestCaseDroid\\target\\classes", "TestCaseDroid.test.CallGraphs", "main");
        CallGraph callGraph = Scene.v().getCallGraph();
        getReachabilityAnalysis(callGraph);
        List<String> callChain = analysis.analyzeCallGraph(callGraph);
        System.out.println(callChain);
    }
}
