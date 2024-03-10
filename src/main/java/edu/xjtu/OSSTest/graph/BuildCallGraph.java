package edu.xjtu.OSSTest.graph;

import edu.xjtu.OSSTest.config.SootConfig;
import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;
import soot.util.dot.DotGraph;

import java.awt.*;
import java.util.Iterator;
import java.util.Map;

import static edu.xjtu.OSSTest.utils.SootUtils.convertDotToPng;
import static edu.xjtu.OSSTest.utils.SootUtils.isExcludedMethod;

public class BuildCallGraph  extends SceneTransformer {

    static DotGraph dotGraph ;
    public static String mainClass = "tests.CallGraph";
    public static void main(String[] args) {

        SootConfig sootConfig = new SootConfig();
        sootConfig.setCallGraphAlgorithm("Spark");
        sootConfig.setupSoot(mainClass, true);

        //add an intra-procedural analysis phase to Soot
        BuildCallGraph analysis = new BuildCallGraph();
        PackManager.v().getPack("wjtp").add(new Transform("wjtp.BuildCallGraph", analysis));

        dotGraph = new DotGraph("callgraph");
        PackManager.v().runPacks();

    }
    @Override
    protected void internalTransform(String phaseName, Map options) {

        int numOfEdges = 0;
        int maxDepth = 10;
        CallGraph callGraph = Scene.v().getCallGraph();
        for(SootClass sc : Scene.v().getApplicationClasses()){
            for(SootMethod m : sc.getMethods()){

                Iterator<MethodOrMethodContext> targets = new Targets(
                        callGraph.edgesOutOf(m));

                int depth = 0;
                while (targets.hasNext() && depth < maxDepth) {
                    SootMethod tgt = (SootMethod) targets.next();
                    if (!isExcludedMethod(tgt)) {
                        numOfEdges++;
                        System.out.println(m + " may call " + tgt);
                        dotGraph.drawEdge(m.toString(), tgt.toString());
                        depth++;
                    }
                }
            }
        }
        System.err.println("Total Edges:" + numOfEdges);

        String callGraphPath = "./sootOutput/dot/"+mainClass+".CG.dot";
        String outputPath= "./sootOutput/pic/"+mainClass+".CG.png";

        dotGraph.plot(callGraphPath);
        try {
            convertDotToPng(callGraphPath, outputPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
