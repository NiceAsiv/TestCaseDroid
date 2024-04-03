package TestCaseDroid.analysis;


import TestCaseDroid.graph.BuildCallGraph;
import TestCaseDroid.graph.BuildCallGraphForJar;
import lombok.Getter;
import lombok.Setter;
import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 使用worklist算法，并在目标函数开始分析，分析函数调用关系，并在逆向达到入口函数时停止分析。
 * <p>这个类使用了worklist算法来分析函数调用关系。worklist算法是一种广度优先搜索算法，它使用一个工作列表来存储待处理的函数。
 * 我们从目标函数开始，逆向寻找到入口函数的路径。我们使用一个栈来存储调用链，因为栈是后进先出的数据结构，这样可以保证我们的路径是逆向的。
 * 我们还使用了一个集合来存储已经访问过的函数，以避免重复处理。</p>
 */
@Getter
@Setter
public class ReachabilityAnalysis {
    private  MethodSig entryMethod;
    private  MethodSig targetMethod;

    /**
     * 构造函数
     *
     * @param targetMethod 目标函数 like "<TestCaseDroid.test.A2: void bar()>"
     * @param entryMethod  入口函数 like "<TestCaseDroid.test.CallGraphs: void main(java.lang.String[])>"
     */
    public ReachabilityAnalysis(String targetMethod, String entryMethod) {
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
        ReachabilityAnalysis analysis = new ReachabilityAnalysis("<TestCaseDroid.test.A2: void bar()>", "<TestCaseDroid.test.CallGraphs: void main(java.lang.String[])>");
        List<String> callChain = analysis.runAnalysis("E:\\Tutorial\\TestCaseDroid\\target\\classes");
        System.out.println(callChain);
    }
}
class MethodSig{
    String signature;
    String className;
    String methodName;
    String returnType;
    List<String> paramTypes;

     /**
     * 构造函数 从函数签名中提取类名、函数名、返回类型和参数类型
     * @param signature 函数签名 like  <TestCaseDroid.test.CallGraphs: void main(java.lang.String[])>
     */
     public MethodSig(String signature) {
        this.signature = signature;
        String pattern = "<(.*): (.*?) (.*?)\\((.*?)\\)>"; //正则表达式
        if (!signature.matches(pattern)) {
            throw new IllegalArgumentException("Invalid method signature: " + signature);
        }
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(signature);
        if (m.find()) {
            this.className = m.group(1);
            this.returnType = m.group(2);
            this.methodName = m.group(3);
            this.paramTypes = new ArrayList<>();
            if (!m.group(4).isEmpty()) {
                String[] paramArray = m.group(4).split(",");
                this.paramTypes.addAll(Arrays.asList(paramArray));
            }
        }else {
            throw new IllegalArgumentException("Invalid method signature: " + signature);
        }
     }

     public static void main(String[] args) {
        MethodSig methodSig = new MethodSig("<TestCaseDroid.test.CallGraphs: void main(java.lang.String[],int)>");
        System.out.println(methodSig.className);
        System.out.println(methodSig.methodName);
        System.out.println(methodSig.returnType);
        System.out.println(methodSig.paramTypes);
     }
}
