package TestCaseDroid.graph;

import TestCaseDroid.config.SootConfig;
import TestCaseDroid.utils.DotGraphWrapper;
import TestCaseDroid.utils.SootUtils;
import com.google.gson.internal.LinkedHashTreeMap;
import lombok.Setter;
import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class BuildCallGraphForJar extends SceneTransformer{

    @Setter
    private static String className;
    private static Map<String, Boolean> visited = new LinkedHashTreeMap<>();
    private static int numOfEdges = 0;
    private static final SootConfig sootConfig = new SootConfig();


    public static void main(String[] args) {
        buildCallGraphForJar("E:\\Tutorial\\TestCaseDroid\\target\\classes", "TestCaseDroid.test.CallGraphs", "doStuff");
    }
    public static void buildCallGraphForJar(String targetJarPath,String callGraphAlgorithm, String className,String entryMethod) {
        sootConfig.setCallGraphAlgorithm(callGraphAlgorithm);
        buildCallGraphForJar(targetJarPath,className,entryMethod);
    }

    public static void buildCallGraphForJar(String targetJarPath,String className,String entryMethod) {
        BuildCallGraphForJar.setClassName(className);
        sootConfig.setupSoot(className, true, targetJarPath);
        //add an inter-procedural analysis phase to Soot
        BuildCallGraphForJar analysis = new BuildCallGraphForJar();
        PackManager.v().getPack("wjtp").add(new Transform("wjtp.BuildCallGraphForJar", analysis));
        SootClass targetClass = Scene.v().getSootClass(className);
        SootMethod entryPoint = targetClass.getMethodByName(entryMethod);
        //check if the mainClass is an application class
        SootUtils.isApplicationClass(targetClass.getName());
        //output the application classes and the function signatures loaded by each class in the current analysis environment
        SootUtils.setEntryPoints(targetClass.getName(), entryPoint.getName());
        //run the analysis
        PackManager.v().runPacks();
    }


    /**
     * Visit the call graph starting from the entry method
     * @param cg call graph
     * @param method entry method
     * @param dotGraph dot graph
     */
    private static void visit(CallGraph cg,SootMethod method, DotGraphWrapper dotGraph)
    {
        String identifier = method.getSignature();
        visited.put(method.getSignature(), true);
        dotGraph.drawNode(identifier);

        Iterator<MethodOrMethodContext> targets = new Targets(cg.edgesOutOf(method));//获取所有被m调用的方法
        while (targets.hasNext()) {
            SootMethod tgt = (SootMethod) targets.next();
            if (SootUtils.isNotExcludedMethod(tgt)&&(method.getDeclaringClass().isApplicationClass()||tgt.getDeclaringClass().isApplicationClass())){
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
    protected void internalTransform(String phaseName, Map<String, String> options) {
        CallGraph callGraph = Scene.v().getCallGraph();
        DotGraphWrapper dotGraph = new DotGraphWrapper("callgraph");
        //获取所有入口函数
        List<SootMethod> sootEntryMethods = Scene.v().getEntryPoints();
        for (SootMethod entryMethod : sootEntryMethods) {
            System.out.println("Entry method: " + entryMethod);
            visited.clear();
            numOfEdges = 0;
            visit(callGraph, entryMethod, dotGraph);
            System.out.println("Total number of edges: " + numOfEdges);
            dotGraph.plot("cg", className+ "." + entryMethod.getName());
        }
    }

}
