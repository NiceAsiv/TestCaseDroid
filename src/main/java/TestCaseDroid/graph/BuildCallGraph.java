package TestCaseDroid.graph;

import TestCaseDroid.config.SootConfig;
import TestCaseDroid.utils.*;
import com.google.gson.internal.LinkedHashTreeMap;
import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;

import java.util.Iterator;
import java.util.Map;

public class BuildCallGraph  extends SceneTransformer {

    public static String mainClass = "TestCaseDroid.test.CallGraphs";
    public static String targetPackageName = "TestCaseDroid";
    private static Map<String, Boolean> visited = new LinkedHashTreeMap<>();
    private static int numOfEdges = 0;
    public static void main(String[] args) {

        SootConfig sootConfig = new SootConfig();
        sootConfig.setCallGraphAlgorithm("Spark");
        sootConfig.setupSoot(mainClass, true);

        //add an inter-procedural analysis phase to Soot
        BuildCallGraph analysis = new BuildCallGraph();
        PackManager.v().getPack("wjtp").add(new Transform("wjtp.BuildCallGraph", analysis));

//        dotGraph = new DotGraph("callgraph");

        //判断mainClass是否为应用类
        SootInfoUtils.isApplicationClass(mainClass);
        //输出当前分析环境下的application类和每个类所加载的函数签名
        SootInfoUtils.reportSootApplicationClassInfo();
        //设置入口方法
        SootAnalysisUtils.setEntryPoints(mainClass,"main");
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
        if (depth >= maxDepth) return;
        while (targets.hasNext()) {
            SootMethod tgt = (SootMethod) targets.next();
            if (SootUtils.isNotExcludedMethod(tgt)&&(tgt.getDeclaringClass().isApplicationClass()||method.getDeclaringClass().isApplicationClass())) {
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
            if (SootUtils.isNotExcludedMethod(tgt)&&(tgt.getDeclaringClass().getName().startsWith(targetPackageName)||method.getDeclaringClass().getName().startsWith(targetPackageName))) {
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
    protected void internalTransform(String phaseName, Map options) {
        CallGraph callGraph = Scene.v().getCallGraph();
        //输出图的大小
        System.out.println("--------------------------------");
        System.out.println("Call graph has " + callGraph.size() + " edges");
        System.out.println("--------------------------------");

        DotGraphWrapper dotGraph = new DotGraphWrapper("callgraph");
        for(SootClass sc : Scene.v().getApplicationClasses()){
            for(SootMethod m : sc.getMethods()){
                visit(callGraph, m, dotGraph);
            }
        }
        System.out.println("Total number of edges: " + numOfEdges);
        dotGraph.plot(mainClass,"cg");



//        for(SootClass sc : Scene.v().getApplicationClasses()){
//            for(SootMethod m : sc.getMethods()){
//                int numOfEdges=0;
//                Boolean hasNextFlag=false;
//                Iterator<MethodOrMethodContext> targets = new Targets(callGraph.edgesOutOf(m)); //获取所有被m调用的方法
//                while (targets.hasNext())
//                {
//                    SootMethod tgt = (SootMethod) targets.next();
//                    if (SootUtils.isNotExcludedMethod(tgt)) {
//                        numOfEdges++;
//                        System.out.println(m + " may call " + tgt);
//                        dotGraph.drawEdge(m.toString(), tgt.toString());
//                        hasNextFlag=true;
//                    }
//                }
//                if(hasNextFlag){
//                    System.out.print(SootVisualizeUtils.TextColor.RED.getCode());
//                    System.out.printf("    %s has %d edges\n",m.getSignature(),numOfEdges);
//                    System.out.print(SootVisualizeUtils.TextColor.RESET.getCode());
//                }
//
//            }
//        }
    }
}
