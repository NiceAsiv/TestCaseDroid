package TestCaseDroid.graph;


import TestCaseDroid.config.SootConfig;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.JimpleBody;
import soot.toolkits.graph.ClassicCompleteUnitGraph;
import soot.util.Chain;


import static TestCaseDroid.utils.SootUtils.UnitGraphToDot;
import static TestCaseDroid.utils.SootUtils.convertDotToPng;

public class BuildControlFlowGraph {
    public static String mainClass = "TestCaseDroid.test.CFGTest";
    public static String targetPackageName = "TestCaseDroid";
    public static void main(String[] args) {
        SootConfig sootConfig = new SootConfig();
        sootConfig.setCallGraphAlgorithm("Spark");
        sootConfig.setupSoot(mainClass, true);
        Chain<SootClass> applicationClasses = soot.Scene.v().getApplicationClasses();
        for (SootClass sc : applicationClasses) {
            for (SootMethod m : sc.getMethods()) {
                if (m.getDeclaringClass().getName().startsWith(targetPackageName)) {
                    JimpleBody jimpleBody = (JimpleBody) m.retrieveActiveBody();
                    String dotFilePath = "./sootOutput/dot/" +mainClass+"."+m.getName() + ".cfg.dot";
                    UnitGraphToDot(new ClassicCompleteUnitGraph(jimpleBody), dotFilePath, m.getName());
                    String pngFilePath = "./sootOutput/pic/" +mainClass+"."+m.getName() + ".cfg.png";
                    convertDotToPng(dotFilePath, pngFilePath);
                }
            }
        }
    }
}
