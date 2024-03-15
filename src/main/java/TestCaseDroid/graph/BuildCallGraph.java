package TestCaseDroid.graph;

import TestCaseDroid.config.SootConfig;
import TestCaseDroid.utils.SootAnalysisUtils;
import TestCaseDroid.utils.SootInfoUtils;
import TestCaseDroid.utils.SootUtils;
import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;
import soot.util.dot.DotGraph;

import java.util.Iterator;
import java.util.Map;

public class BuildCallGraph  extends SceneTransformer {

    static DotGraph dotGraph ;
    public static String mainClass = "TestCaseDroid.test.FastJsonTest";
    public static String targetPackageName = "TestCaseDroid";
    public static void main(String[] args) {

        SootConfig sootConfig = new SootConfig();
        sootConfig.setCallGraphAlgorithm("Spark");
        sootConfig.setupSoot(mainClass, true);

        //add an intra-procedural analysis phase to Soot
        BuildCallGraph analysis = new BuildCallGraph();
        PackManager.v().getPack("wjtp").add(new Transform("wjtp.BuildCallGraph", analysis));

        dotGraph = new DotGraph("callgraph");

        //判断mainClass是否为应用类
        SootInfoUtils.isApplicationClass(mainClass);
        //输出当前分析环境下的application类和每个类所加载的函数签名
        SootInfoUtils.reportSootApplicationClassInfo();
        //设置入口方法
        SootAnalysisUtils.setEntryPoints(mainClass,"main","testFunction");
        PackManager.v().runPacks();

    }
    @Override
    protected void internalTransform(String phaseName, Map options) {

        int numOfEdges = 0;
        int maxDepth = 10;
        CallGraph callGraph = Scene.v().getCallGraph();
        for(SootClass sc : Scene.v().getApplicationClasses()){
            for(SootMethod m : sc.getMethods()){

                Iterator<MethodOrMethodContext> targets = new Targets(callGraph.edgesOutOf(m)); //获取所有被m调用的方法

//                int depth = 0;
//                while (targets.hasNext() && depth < maxDepth) {
//                    SootMethod tgt = (SootMethod) targets.next();
//                    if (!isExcludedMethod(tgt)&&tgt.getDeclaringClass().getName().startsWith(targetPackageName)) {
//                        numOfEdges++;
//                        System.out.println(m + " may call " + tgt);
//                        dotGraph.drawEdge(m.toString(), tgt.toString());
//                        depth++;
//                    }
//                }
                while (targets.hasNext())
                {
                    SootMethod tgt = (SootMethod) targets.next();
                    if (SootUtils.isNotExcludedMethod(tgt)) {
                        numOfEdges++;
                        System.out.println(m + " may call " + tgt);
                        dotGraph.drawEdge(m.toString(), tgt.toString());
                    }
                }
            }
        }
        System.err.println("Total Edges:" + numOfEdges);

        String callGraphPath = "./sootOutput/dot/"+mainClass+".cg.dot";
        String outputPath= "./sootOutput/pic/"+mainClass+".cg.png";

        dotGraph.plot(callGraphPath);
        try {
            SootUtils.convertDotToPng(callGraphPath, outputPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
