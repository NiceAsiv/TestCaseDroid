package TestCaseDroid.graph;

import TestCaseDroid.config.SootConfig;
import TestCaseDroid.utils.DotGraphWrapper;
import TestCaseDroid.utils.SootAnalysisUtils;
import TestCaseDroid.utils.SootInfoUtils;
import TestCaseDroid.utils.SootUtils;
import com.google.gson.internal.LinkedHashTreeMap;
import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class BuildCallGraphForJar extends SceneTransformer{

    public static String targetPackageName = "TestCaseDroid";
    private static Map<String, Boolean> visited = new LinkedHashTreeMap<>();
    private static int numOfEdges = 0;
    List<SootMethod> callChain = new ArrayList<>();


    public static void main(String[] args) {
        buildCallGraphForJar("E:\\Tutorial\\TestCaseDroid\\target\\classes", "TestCaseDroid.test.CallGraphs", "doStuff");
    }

    public static void buildCallGraphForJar(String targetJarPath,String className,String entryMethod) {
        SootConfig sootConfig = new SootConfig();
        sootConfig.setCallGraphAlgorithm("Spark");
        sootConfig.setupSoot(className, true, targetJarPath);

        //add an inter-procedural analysis phase to Soot
        BuildCallGraphForJar analysis = new BuildCallGraphForJar();
        PackManager.v().getPack("wjtp").add(new Transform("wjtp.BuildCallGraphForJar", analysis));
//        SootClass targetClass = Scene.v().getSootClass("NotepadV2");

        SootClass targetClass = Scene.v().getSootClass(className);
        SootMethod entryPoint = targetClass.getMethodByName(entryMethod);
        //判断mainClass是否为应用类
        SootInfoUtils.isApplicationClass(targetClass.getName());
        //输出当前分析环境下的application类和每个类所加载的函数签名
        SootAnalysisUtils.setEntryPoints(targetClass.getName(), entryPoint.getName());
        //运行分析
        PackManager.v().runPacks();
    }


    /**
     * 递归遍历call graph,并且限制深度
     * 为了避免重复遍历，使用visited map记录已经遍历过的方法
     * @param method 当前方法
     * @param cg call graph
     * @param dotGraph dot图
     * @param depth 当前深度
     * @param maxDepth 最大深度
     */
    private static void visit(CallGraph cg,SootMethod method, DotGraphWrapper dotGraph , int depth, int maxDepth)
    {

        String identifier = method.getSignature();
        visited.put(method.getSignature(), true);
        dotGraph.drawNode(identifier);
        Iterator<MethodOrMethodContext> targets = new Targets(cg.edgesOutOf(method));//获取所有被m调用的方法
        if (depth >= maxDepth) {
            return;
        }
        while (targets.hasNext()) {
            SootMethod tgt = (SootMethod) targets.next();
            if (SootUtils.isNotExcludedMethod(tgt)&&(method.getDeclaringClass().isApplicationClass()||tgt.getDeclaringClass().isApplicationClass())) {
                String tgtIdentifier = tgt.getSignature();
                if (!visited.containsKey(tgtIdentifier)) {
                    dotGraph.drawNode(tgtIdentifier);
                    dotGraph.drawEdge(identifier, tgtIdentifier);
                    System.out.println(method + " may call " + tgt);
                    numOfEdges++;
                    visit(cg, tgt, dotGraph, depth + 1, maxDepth);
                }
            }
        }
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
            if (SootUtils.isNotExcludedMethod(tgt)&&(method.getDeclaringClass().isApplicationClass()||tgt.getDeclaringClass().isApplicationClass())){
                String tgtIdentifier = tgt.getSignature();
                if (!visited.containsKey(tgtIdentifier)) {
                    dotGraph.drawNode(tgtIdentifier);
                    dotGraph.drawEdge(identifier, tgtIdentifier);
                    System.out.println(method + " may call " + tgt);
                    numOfEdges++;
                    visit(cg, tgt, dotGraph);
                }
            }
        }
    }
    @Override
    protected void internalTransform(String phaseName, Map<String, String> options) {
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
            dotGraph.plot("cg", targetPackageName + "." + entryMethod.getName());
        }
    }

}
