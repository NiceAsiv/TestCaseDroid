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
    public static String targetPackageName = "TestCaseDroid";
    public static String mainClass = "TestCaseDroid.test.CallGraph";
    public static String entryMethod = "main";
    public static String mode = "class";
    private static Map<String, Boolean> visited = new LinkedHashTreeMap<>();
    private static int numOfEdges = 0;
    public static void main(String[] args) {
//        if (args.length == 0) {
//            System.out.println("Usage: java BuildCallGraph <mode>");
//            System.out.println("mode: jar/class");
//            return;
//        }
        BuildCallGraph buildCallGraph = new BuildCallGraph(mode);
    }
    BuildCallGraph(String mode) {
        if (mode.equals("jar")) {
            buildCallGraphForJar();
        } else if (mode.equals("class")) {
            buildCallGraphForClass();
        }else {
            System.out.println("Invalid mode");
        }
    }
    BuildCallGraph() {
    }

    public static void buildCallGraphForClass() {

        SootConfig sootConfig = new SootConfig();
        sootConfig.setCallGraphAlgorithm("Spark");
        sootConfig.setupSoot(mainClass, true);

        //add an inter-procedural analysis phase to Soot
        BuildCallGraph analysis = new BuildCallGraph();
        PackManager.v().getPack("wjtp").add(new Transform("wjtp.BuildCallGraph", analysis));

        //判断mainClass是否为应用类
        SootInfoUtils.isApplicationClass(mainClass);
        //输出当前分析环境下的application类和每个类所加载的函数签名
        SootInfoUtils.reportSootApplicationClassInfo();
        //设置入口方法
        SootAnalysisUtils.setEntryPoints(mainClass, entryMethod);
        //运行分析
        PackManager.v().runPacks();
    }

    public static void buildCallGraphForJar() {
        SootConfig sootConfig = new SootConfig();
        sootConfig.setCallGraphAlgorithm("Spark");
        sootConfig.setupSootForJar("./jarCollection/NotepadV2.jar", true);

        //add an inter-procedural analysis phase to Soot
        BuildCallGraph analysis = new BuildCallGraph();
        PackManager.v().getPack("wjtp").add(new Transform("wjtp.BuildCallGraph", analysis));
//        SootClass targetClass = Scene.v().getSootClass("NotepadV2");
        if (Scene.v().hasMainClass()) {
            SootClass targetClass = Scene.v().getMainClass();
            //判断mainClass是否为应用类
            SootInfoUtils.isApplicationClass(targetClass.getName());
            //输出当前分析环境下的application类和每个类所加载的函数签名
            SootInfoUtils.reportSootApplicationClassInfo();
            //设置入口方法
            SootMethod entryPoint = targetClass.getMethodByName("main");
            SootAnalysisUtils.setEntryPoints(targetClass.getName(), entryPoint.getName());
        }
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
            if (SootUtils.isNotExcludedMethod(tgt)&&isNotFilteredMethod(method,tgt)) {
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
            if (SootUtils.isNotExcludedMethod(tgt)&&isNotFilteredMethod(method,tgt)) {
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
//        for(SootClass sc : Scene.v().getApplicationClasses()){
//            for(SootMethod m : sc.getMethods()){
////                //如果需要同时分析两个入口函数并画在一张图中，应当在画每一个函数的时候清空visited，需要解除下面的注释
////                visited.clear();
//                visit(callGraph, m, dotGraph);
//            }
//        }
        if (Scene.v().hasMainClass()) {
            targetPackageName = Scene.v().getMainClass().getPackageName();
            SootClass targetClass = Scene.v().getMainClass();
            SootMethod entryPoint = targetClass.getMethodByName("main");
            visit(callGraph, entryPoint, dotGraph);
        }
        System.out.println("Total number of edges: " + numOfEdges);
        dotGraph.plot(mainClass,"cg");

    }
    public static Boolean isNotFilteredMethod(SootMethod method, SootMethod tgt){
        String methodName = method.getName();
        String className = method.getDeclaringClass().getName();
        if(mode.equals("jar") &&(method.getDeclaringClass().isApplicationClass()||tgt.getDeclaringClass().isApplicationClass()))
            return true;
        else return mode.equals("class") && (className.startsWith(targetPackageName) || tgt.getDeclaringClass().getName().startsWith(targetPackageName));
    }

}
