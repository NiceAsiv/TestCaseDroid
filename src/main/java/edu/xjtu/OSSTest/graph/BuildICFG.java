package edu.xjtu.OSSTest.graph;

import edu.xjtu.OSSTest.config.SootConfig;
import heros.InterproceduralCFG;
import soot.*;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.graph.UnitGraph;
import soot.util.dot.DotGraph;

import java.util.Map;

import static edu.xjtu.OSSTest.utils.SootUtils.convertDotToPng;

public class BuildICFG extends SceneTransformer {

    static DotGraph dotGraph ;
    public static String mainClass = "edu.xjtu.OSSTest.test.MainCFA";
    public static String targetPackageName = "edu.xjtu.OSSTest";

    public static void main(String[] args) {
        SootConfig sootConfig = new SootConfig();
        sootConfig.setCallGraphAlgorithm("Spark");
        sootConfig.setupSoot(mainClass, true);

        //add an intra-procedural analysis phase to Soot
        BuildICFG analysis = new BuildICFG();
        PackManager.v().getPack("wjtp").add(new Transform("wjtp.BuildICFG", analysis));
        dotGraph = new DotGraph("icfg");
        PackManager.v().runPacks();
    }

    @Override
    protected void internalTransform(String phaseName, Map<String, String> options) {

        JimpleBasedInterproceduralCFG icfg = new JimpleBasedInterproceduralCFG();
        for(SootClass sc : Scene.v().getApplicationClasses()){
            for(SootMethod m : sc.getMethods()){
                if(m.hasActiveBody()){
                    UnitGraph g = new ExceptionalUnitGraph(m.getActiveBody());
                    for(Unit u : g){
                        for(Unit v : icfg.getSuccsOf(u)){
                            System.out.println(u + " may reach " + v);
                            dotGraph.drawEdge(u.toString(), v.toString());
                        }
                    }
                }
            }
        }

        String dotFilePath = "./sootOutput/dot/"+ mainClass + ".icfg.dot";
        dotGraph.plot(dotFilePath);
        String outputFilePath = "./sootOutput/pic/"+ mainClass + ".icfg.png";
        dotGraph.plot(outputFilePath);

        try {
            convertDotToPng(dotFilePath, outputFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
