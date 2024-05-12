package TestCaseDroid.graph;

import TestCaseDroid.analysis.reachability.MethodContext;
import TestCaseDroid.config.SootConfig;
import TestCaseDroid.utils.DotGraphWrapper;
import TestCaseDroid.utils.SootUtils;
import com.google.gson.internal.LinkedHashTreeMap;
import lombok.Setter;
import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;

import java.util.*;


public class BuildCallGraphForJar extends SceneTransformer{

    @Setter
    private static String className;
    private static Set<String> visitedEdges = new HashSet<>();
    private static int numOfEdges = 0;
    private static final SootConfig sootConfig = new SootConfig();


    public static void main(String[] args) {
        buildCallGraphForJar("E:\\Tutorial\\TestCaseDroid\\target\\classes", "TestCaseDroid.test.Vulnerable", new MethodContext("<TestCaseDroid.test.Vulnerable: void main(java.lang.String[])>"));
    }
    public static void buildCallGraphForJar(String targetJarPath,String callGraphAlgorithm, String className,MethodContext entryMethod) {
        sootConfig.setCallGraphAlgorithm(callGraphAlgorithm);
        buildCallGraphForJar(targetJarPath,className,entryMethod);
    }

    public static void buildCallGraphForJar(String targetJarPath, String className, MethodContext entryMethod) {
        BuildCallGraphForJar.setClassName(className);
        sootConfig.setupSoot(className, true, targetJarPath);
        //add an inter-procedural analysis phase to Soot
        BuildCallGraphForJar analysis = new BuildCallGraphForJar();
        PackManager.v().getPack("wjtp").add(new Transform("wjtp.BuildCallGraphForJar", analysis));
        SootClass targetClass = Scene.v().getSootClass(className);
        //check if the mainClass is an application class
        SootUtils.isApplicationClass(targetClass.getName());
        //output the application classes and the function signatures loaded by each class in the current analysis environment
        SootUtils.setEntryPoints(targetClass.getName(), entryMethod.getMethodSignature());
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
        Queue<SootMethod> worklist = new LinkedList<>(); //这是一个队列，用于广度优先搜索
        String identifier = method.getSignature();
        worklist.add(method);
        dotGraph.drawNode(identifier);
        while (!worklist.isEmpty()) {
            SootMethod m = worklist.poll();
            Iterator<MethodOrMethodContext> targets = new Targets(cg.edgesOutOf(m));
            while (targets.hasNext()) {
                SootMethod tgt = (SootMethod) targets.next();
                if (SootUtils.isNotExcludedMethod(tgt)&&!tgt.isJavaLibraryMethod()){
                    String tgtIdentifier = tgt.getSignature();
                    String edge = m.getSignature() + "->" + tgt.getSignature();
                    dotGraph.drawNode(tgtIdentifier);
                    if (!visitedEdges.contains(edge))
                    {
                        dotGraph.drawEdge(m.getSignature(), tgtIdentifier);
                        System.out.println(m + " may call " + tgt);
                        numOfEdges++;
                        worklist.add(tgt);
                        visitedEdges.add(edge);
                    }
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
            visitedEdges.clear();
            numOfEdges = 0;
            visit(callGraph, entryMethod, dotGraph);
            System.out.println("Total number of edges: " + numOfEdges);
            dotGraph.plot("cg", className+ "." + entryMethod.getName());
        }
    }

}
