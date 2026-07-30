package TestCaseDroid.graph;

import TestCaseDroid.analysis.reachability.MethodContext;
import TestCaseDroid.config.SootConfig;
import TestCaseDroid.utils.DotGraphWrapper;
import TestCaseDroid.utils.SootUtils;
import soot.MethodOrMethodContext;
import soot.Scene;
import soot.SootMethod;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Queue;
import java.util.Set;

/** Builds and exports a call graph rooted at one method. */
public final class BuildCallGraphForJar {
    private BuildCallGraphForJar() {
    }

    public static void buildCallGraphForJar(String targetPath, String callGraphAlgorithm,
                                             String entryClassName, MethodContext entryMethod) {
        SootConfig config = new SootConfig();
        config.setCallGraphAlgorithm(callGraphAlgorithm);
        build(targetPath, entryClassName, entryMethod, config);
    }

    public static void buildCallGraphForJar(String targetPath, String entryClassName,
                                             MethodContext entryMethod) {
        build(targetPath, entryClassName, entryMethod, new SootConfig());
    }

    private static void build(String targetPath, String entryClassName,
                              MethodContext entryMethod, SootConfig config) {
        if (entryMethod == null) {
            throw new IllegalArgumentException("Entry method must not be null");
        }
        config.setupSoot(entryClassName, true, targetPath,
                Collections.singletonList(entryMethod.getMethodSignature()));

        SootMethod root = Scene.v().getMethod(entryMethod.getMethodSignature());
        DotGraphWrapper graph = new DotGraphWrapper(
                "The call graph of " + entryMethod.getClassName() + "." + entryMethod.getMethodName());
        int edges = visit(Scene.v().getCallGraph(), root, graph);
        System.out.println("Entry method: " + root);
        System.out.println("Total number of edges: " + edges);
        graph.plot("cg", entryMethod.getClassName() + "." + entryMethod.getMethodName());
    }

    private static int visit(CallGraph callGraph, SootMethod root, DotGraphWrapper graph) {
        Queue<SootMethod> worklist = new ArrayDeque<>();
        Set<String> visitedMethods = new HashSet<>();
        Set<String> visitedEdges = new HashSet<>();
        worklist.add(root);
        graph.drawNode(root.getSignature());
        int edgeCount = 0;

        while (!worklist.isEmpty()) {
            SootMethod source = worklist.remove();
            if (!visitedMethods.add(source.getSignature())) {
                continue;
            }
            Iterator<MethodOrMethodContext> targets = new Targets(callGraph.edgesOutOf(source));
            while (targets.hasNext()) {
                SootMethod target = (SootMethod) targets.next();
                if (target.isJavaLibraryMethod() || !SootUtils.isNotExcludedMethod(target)) {
                    continue;
                }
                String edge = source.getSignature() + " -> " + target.getSignature();
                if (visitedEdges.add(edge)) {
                    graph.drawNode(target.getSignature());
                    graph.drawEdge(source.getSignature(), target.getSignature());
                    System.out.println(source + " may call " + target);
                    edgeCount++;
                }
                if (!visitedMethods.contains(target.getSignature())) {
                    worklist.add(target);
                }
            }
        }
        return edgeCount;
    }
}
