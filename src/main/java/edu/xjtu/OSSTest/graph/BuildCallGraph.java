package edu.xjtu.OSSTest.graph;

import edu.xjtu.OSSTest.config.SootConfig;
import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;
import soot.util.dot.DotGraph;
import soot.util.dot.DotGraphNode;

import java.util.Iterator;
import java.util.Map;

import static edu.xjtu.OSSTest.utils.SootUtils.convertDotToPng;
import static edu.xjtu.OSSTest.utils.SootUtils.isExcludedMethod;

public class BuildCallGraph  extends SceneTransformer {

    static DotGraph dotGraph ;

    public static String mainClass;
    public static void main(String[] args) {
        String mainClass = "tests.CallGraph";
        BuildCallGraph.mainClass = mainClass;
        SootConfig sootConfig = new SootConfig();
        sootConfig.setCallGraphAlgorithm("Spark");
        sootConfig.setupSoot(mainClass, true);

        //add an intra-procedural edu.xjtu.OSSTest.analysis phase to Soot
        BuildCallGraph analysis = new BuildCallGraph();
        PackManager.v().getPack("wjtp").add(new Transform("wjtp.BuildCallGraph", analysis));

        dotGraph = new DotGraph("callgraph");

        PackManager.v().runPacks();

    }
    @Override
    protected void internalTransform(String phaseName, Map options) {

        int numOfEdges = 0;
        int maxDepth = 5;
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
                        depth++;
                        System.out.println(m + " may call " + tgt);
                        DotGraphNode srcNode = dotGraph.drawNode(m.toString());
                        DotGraphNode tgtNode = dotGraph.drawNode(tgt.toString());
                        srcNode.setAttribute("shape", "box");
                        tgtNode.setAttribute("shape", "box");
                        if (numOfEdges == 1) {
                            srcNode.setAttribute("color", "gray");
                            srcNode.setAttribute("style", "filled");
                        }
                        //set font
                        srcNode.setAttribute("fontname", "JetBrains Mono");
                        tgtNode.setAttribute("fontname", "JetBrains Mono");
                        dotGraph.drawEdge(m.toString(), tgt.toString());
                    }
                }
            }
        }
        System.err.println("Total Edges:" + numOfEdges);
        String dotFileName = BuildCallGraph.mainClass + ".dot";
        dotGraph.setGraphLabel(BuildCallGraph.mainClass + " call graph");
        dotGraph.plot("./sootOutput/dot/" + dotFileName);
        try {
            convertDotToPng("./sootOutput/dot/" + dotFileName, "./sootOutput/pic/" + BuildCallGraph.mainClass + ".png");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
