package TestCaseDroid.graph;

import TestCaseDroid.config.SootConfig;
import TestCaseDroid.utils.*;
import com.google.gson.internal.LinkedHashTreeMap;
import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Sources;
import soot.jimple.toolkits.callgraph.Targets;

import java.util.*;

public class BuildCallGraph  extends SceneTransformer {
    private static String targetClass = "TestCaseDroid.test.Vulnerable";
    public static String entryMethod = "main";
    private static Map<String, Boolean> visited = new LinkedHashTreeMap<>();
    private static int numOfEdges = 0;
    private static final SootConfig sootConfig = new SootConfig();



    public static void main(String[] args) {
        buildCallGraph();
    }

    public static void buildCallGraph(String callGraphAlgorithm, String className,String entryMethod) {
          targetClass = className;
          BuildCallGraph.entryMethod = entryMethod;
          sootConfig.setCallGraphAlgorithm(callGraphAlgorithm);
          buildCallGraph();
    }
    public static void buildCallGraph() {
        sootConfig.setupSoot(targetClass, true);

        //add an inter-procedural analysis phase to Soot
        BuildCallGraph analysis = new BuildCallGraph();
        PackManager.v().getPack("wjtp").add(new Transform("wjtp.BuildCallGraph", analysis));

        //判断mainClass是否为应用类
        SootUtils.isApplicationClass(targetClass);
        //输出当前分析环境下的application类和每个类所加载的函数签名
        SootUtils.reportSootApplicationClassInfo();
        //设置入口方法
        SootUtils.setEntryPoints(targetClass, entryMethod);
        //运行分析
        PackManager.v().runPacks();
    }


    @Override
    protected void internalTransform(String phaseName, Map options) {
        CallGraph callGraph = Scene.v().getCallGraph();
        DotGraphWrapper dotGraph = new DotGraphWrapper("callgraph");
        //获取所有入口函数
        List<SootMethod> sootEntryMethods = Scene.v().getEntryPoints();

        for (SootMethod entryMethod : sootEntryMethods) {
            System.out.println("Entry method: " + entryMethod);
            visited.clear();
            numOfEdges = 0;
            visit(callGraph, entryMethod, dotGraph);
            System.out.println("Total number of edges: " + numOfEdges);
            dotGraph.plot("cg", targetClass + "." + entryMethod.getName());
        }
    }
    public static CallGraph getCallGraph() {
        return Scene.v().getCallGraph();
    }
    /**
     * 递归遍历call graph
     * 为了避免重复遍历，使用visited map记录已经遍历过的方法
     * @param cg call graph
     * @param method 当前方法
     * @param dotGraph dot图
     */
    private static void visit(CallGraph cg,SootMethod method, DotGraphWrapper dotGraph)
    {
        String identifier = method.getSignature();
        visited.put(method.getSignature(), true);
        dotGraph.drawNode(identifier);

        Iterator<MethodOrMethodContext> targets = new Targets(cg.edgesOutOf(method));//获取所有被m调用的方法
        while (targets.hasNext()) {
            SootMethod tgt = (SootMethod) targets.next();
            if (SootUtils.isNotExcludedMethod(tgt)&& (method.getDeclaringClass().isApplicationClass()||tgt.getDeclaringClass().isApplicationClass())) {
                String tgtIdentifier = tgt.getSignature();
                if (!visited.containsKey(tgtIdentifier)&&!tgt.isJavaLibraryMethod()) {
                    dotGraph.drawNode(tgtIdentifier);
                    dotGraph.drawEdge(identifier, tgtIdentifier);
                    System.out.println(method + " may call " + tgt);
                    numOfEdges++;
                    visit(cg, tgt, dotGraph);
                }
            }
        }
    }
}
