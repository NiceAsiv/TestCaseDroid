package TestCaseDroid.graph;


import TestCaseDroid.config.SootConfig;
import TestCaseDroid.utils.DotGraphWrapper;
import TestCaseDroid.utils.SootDataProcessUtils;
import soot.SootClass;
import soot.SootMethod;
import soot.jimple.JimpleBody;
import soot.toolkits.graph.ClassicCompleteUnitGraph;
import soot.util.Chain;

import static TestCaseDroid.utils.SootUtils.UnitGraphToDot;

public class BuildControlFlowGraph {
    public static String mainClass = "TestCaseDroid.test.CFGTest";
    public static DotGraphWrapper dotGraph = new DotGraphWrapper("controlFlowGraph");

    public static void main(String[] args) {
        //配置soot
        SootConfig sootConfig = new SootConfig();
        sootConfig.setCallGraphAlgorithm("Spark");
        sootConfig.setupSoot(mainClass, false);
        //获取应用类链
        Chain<SootClass> applicationClasses = soot.Scene.v().getApplicationClasses();
        for (SootClass sc : applicationClasses) {
            for (SootMethod m : sc.getMethods()) {
                //获取方法的Jimple体
                JimpleBody jimpleBody = (JimpleBody) m.retrieveActiveBody();
                //去除非法字符
                String className = SootDataProcessUtils.removeIllegalCharacters(sc.getName());
                String methodName = SootDataProcessUtils.removeIllegalCharacters(m.getName());
                //生成dot文件
                String dotFilePath = "./sootOutput/dot/" + className + "." + methodName + ".cfg.dot";
                UnitGraphToDot(new ClassicCompleteUnitGraph(jimpleBody), dotFilePath, methodName);
                //生成png文件
                String inputMethodName = className + "." + methodName;
                dotGraph.plot(inputMethodName, "cfg");

            }
        }
    }
}
