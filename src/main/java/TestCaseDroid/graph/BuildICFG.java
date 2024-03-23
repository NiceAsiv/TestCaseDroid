package TestCaseDroid.graph;

import TestCaseDroid.config.SootConfig;
import TestCaseDroid.utils.SootUtils;
import heros.InterproceduralCFG;
import soot.*;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;
import soot.util.dot.DotGraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class BuildICFG extends SceneTransformer {

    static DotGraph dotGraph ;
    public static String mainClass = "TestCaseDroid.test.FastJsonTest";
    public static String targetJarPath = "./jarCollection/soot-1.0.jar";
    public static String targetPackageName = "TestCaseDroid";
    private ArrayList<Unit> visited;


    public BuildICFG(String inputFileType,String FilePath) {

    }

    public static void main(String[] args) {
        SootConfig sootConfig = new SootConfig();
        sootConfig.setCallGraphAlgorithm("Spark");
//        sootConfig.setupSootForJar(targetJarPath, true);
        sootConfig.setupSootForJar(targetJarPath, true);
        //add an intra-procedural analysis phase to Soot
        BuildICFG analysis = new BuildICFG("jar",targetJarPath);
        PackManager.v().getPack("wjtp").add(new Transform("wjtp.ifds", analysis));
        dotGraph = new DotGraph("icfg");
        PackManager.v().runPacks();
    }

    @Override
    protected void internalTransform(String phaseName, Map<String, String> options) {
        if (Scene.v().hasMainClass()) {
            SootMethod mainMethod = Scene.v().getMainMethod();
            Scene.v().setEntryPoints(Collections.singletonList(mainMethod));
            JimpleBasedInterproceduralCFG icfg = new JimpleBasedInterproceduralCFG();
            visited = new ArrayList<>();
            if (mainMethod.hasActiveBody()) {
                Unit startPoint = mainMethod.getActiveBody().getUnits().getFirst();
                graphTraverse(startPoint, icfg);
            }
        }else {
            System.out.println("No main method found!");
            return;
        }

        String dotFilePath = "./sootOutput/dot/"+ mainClass + ".icfg.dot";
        dotGraph.plot(dotFilePath);
        String outputFilePath = "./sootOutput/pic/"+ mainClass + ".icfg.png";
        try {
            SootUtils.convertDotToPng(dotFilePath, outputFilePath);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void graphTraverse(final Unit startPoint, final InterproceduralCFG<Unit, SootMethod> icfg) {
        final List<Unit> currentSuccessors = icfg.getSuccsOf(startPoint);
        if (currentSuccessors.isEmpty()) {
            System.out.println("Traversal complete");
            return;
        }
        for (final Unit succ : currentSuccessors) {
            if (!this.visited.contains(succ)) {
                dotGraph.drawEdge(startPoint.toString(), succ.toString());
                System.out.println(startPoint + " may reach " + succ);
                this.visited.add(succ);
                this.graphTraverse(succ, icfg);
            }
            else {
                dotGraph.drawEdge(startPoint.toString(), succ.toString());
                System.out.println(startPoint +"may reach " + succ);
            }
        }
    }
}
