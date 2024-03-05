package graph;

import config.SootConfig;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;

public class BuildCallGraph {
    public static String  className;
    public static String methodName;

    public BuildCallGraph(String className, String methodName) throws Exception {
        BuildCallGraph.className = className;
        BuildCallGraph.methodName = methodName;
        SootConfig.setupSoot(className, true);
        buildCG();
    }


    public static void buildCG() throws Exception {
        SootClass sootClass = Scene.v().getSootClass(className);
        if (sootClass == null) {
            throw new Exception("Class not found: " + className);
        }
        SootMethod sootMethod = sootClass.getMethodByName(methodName);
        if (sootMethod == null) {
            throw new Exception("Method not found: " + methodName);
        }






        SootConfig.getBasicInfo();
        SootConfig.addExcludeClassesList();
    }




}
