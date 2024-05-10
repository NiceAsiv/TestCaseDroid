package TestCaseDroid.graph;

import TestCaseDroid.analysis.reachability.MethodContext;
import TestCaseDroid.config.SootConfig;
import TestCaseDroid.utils.DotGraphWrapper;
import heros.InterproceduralCFG;
import lombok.Setter;
import soot.*;
import soot.jimple.InvokeExpr;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


@Setter
public class BuildICFG extends SceneTransformer {

    private static String targetClassName = "TestCaseDroid.test.Vulnerable";
    private static MethodContext methodEntryContext;
    private static DotGraphWrapper dotGraph = new DotGraphWrapper("interproceduralCFG");
    private ArrayList<Unit> visited;

    public static void main(String[] args) {
        String targetClassName = "TestCaseDroid.test.Vulnerable";
        String entryMethod = "<TestCaseDroid.test.Vulnerable: void main(java.lang.String[])>";
        MethodContext methodEntryContext = new MethodContext(entryMethod);
        buildICFGForClass(null, targetClassName, methodEntryContext);
    }

    public static void buildICFGForClass(String inputFilePath, String classNameForAnalysis, MethodContext methodEntryContext) {
        SootConfig sootConfig = new SootConfig();
        if (inputFilePath != null) {
            sootConfig.setupSoot(classNameForAnalysis, true, inputFilePath);
        } else {
            sootConfig.setupSoot(classNameForAnalysis, true);
        }
        targetClassName = classNameForAnalysis;
        BuildICFG.methodEntryContext = methodEntryContext;
        sootConfig.setCallGraphAlgorithm("CHA");

        //add an intra-procedural analysis phase to Soot
        BuildICFG analysis = new BuildICFG();
        PackManager.v().getPack("wjtp").add(new Transform("wjtp.ifds", analysis));
        PackManager.v().runPacks();
    }

    @Override
    protected void internalTransform(String phaseName, Map<String, String> options) {
        SootMethod targetMethod = Scene.v().getMethod(methodEntryContext.getMethodSignature());
        if (targetMethod.hasActiveBody()) {
            JimpleBasedInterproceduralCFG icfg = new JimpleBasedInterproceduralCFG();
            visited = new ArrayList<>();
            Unit startPoint = targetMethod.getActiveBody().getUnits().getFirst();
            Unit returnPoint = targetMethod.getActiveBody().getUnits().getLast();
            graphTraverse(startPoint,returnPoint,icfg);
        }else
        {
            System.out.println("No active body for method " + targetMethod);
        }
        dotGraph.plot("icfg", targetClassName, targetMethod.getName());
    }

    public void graphTraverse(final Unit startPoint,final Unit returnPoint,final InterproceduralCFG<Unit, SootMethod> icfg) {
        final List<Unit> currentSuccessors = icfg.getSuccsOf(startPoint);
        if (currentSuccessors.isEmpty()) {
            System.out.println("Traversal reached a dead end at " + startPoint);
            return;
        }
        //如果是函数调用语句，还要遍历函数体
        if (startPoint instanceof soot.jimple.InvokeStmt) {
            final InvokeExpr invokeExpr = ((soot.jimple.InvokeStmt) startPoint).getInvokeExpr();
            final SootMethod targetMethod = invokeExpr.getMethod();
            System.out.println("Encountered a call to " + targetMethod);
            if (targetMethod.hasActiveBody()) {
                final Unit targetStartPoint = targetMethod.getActiveBody().getUnits().getFirst();
                if (!this.visited.contains(targetStartPoint)) {
                    dotGraph.drawEdge(startPoint.toString(), targetStartPoint.toString());
                    System.out.println(startPoint + " may reach " + targetStartPoint);
                    this.visited.add(targetStartPoint);
                    this.graphTraverse(targetStartPoint, returnPoint, icfg);
                    if (returnPoint != null)
                    {
                        dotGraph.drawEdge(targetStartPoint.toString(), returnPoint.toString());
                        System.out.println(targetStartPoint + " may reach " + returnPoint);
                    }
                }
                else {
                    dotGraph.drawEdge(startPoint.toString(), targetStartPoint.toString());
                    System.out.println(startPoint + " may reach " + targetStartPoint);
                }
            }
        }
        for (final Unit succ : currentSuccessors) {
            if (!this.visited.contains(succ)) {
                dotGraph.drawEdge(startPoint.toString(), succ.toString());
                System.out.println(startPoint + " may reach " + succ);
                this.visited.add(succ);
                this.graphTraverse(succ,returnPoint, icfg);
            }
            else {
                dotGraph.drawEdge(startPoint.toString(), succ.toString());
                System.out.println(startPoint +"may reach " + succ);
            }
        }

    }

}
