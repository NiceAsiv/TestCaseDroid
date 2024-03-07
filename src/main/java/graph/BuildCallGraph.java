package graph;

import config.SootConfig;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;

import java.lang.annotation.Target;

public class BuildCallGraph {
    public static String  targetClassName;
    public static String entryMethodName;

    public BuildCallGraph(String className, String methodName) throws Exception {
        BuildCallGraph.targetClassName =   className;
        BuildCallGraph.entryMethodName = methodName;
        SootConfig.setupSoot(className, true);
        buildCG();
    }


    public static void buildCG() throws Exception {

        SootClass sootClass = Scene.v().getSootClass(targetClassName);
        if (sootClass == null) {
            throw new Exception("Class not found: " + targetClassName);
        }
        SootMethod sootMethod = sootClass.getMethodByName(entryMethodName);
        if (sootMethod == null) {
            throw new Exception("Method not found: " + entryMethodName);
        }




        SootConfig.getBasicInfo();
        SootConfig.addExcludeClassesList();
    }




}
