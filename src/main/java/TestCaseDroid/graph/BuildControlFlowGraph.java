package TestCaseDroid.graph;

import TestCaseDroid.analysis.reachability.MethodContext;
import TestCaseDroid.config.SootConfig;
import TestCaseDroid.utils.DotGraphWrapper;
import lombok.Setter;
import soot.*;
import soot.jimple.JimpleBody;
import soot.jimple.ReturnStmt;
import soot.toolkits.graph.*;
import soot.util.cfgcmd.CFGGraphType;
import soot.util.cfgcmd.CFGToDotGraph;
import soot.util.dot.DotGraph;
import soot.util.dot.DotGraphNode;

import java.util.*;

import static TestCaseDroid.utils.FileUtils.folderExistenceTest;


@Setter
public class BuildControlFlowGraph {
    private static DotGraphWrapper dotGraph;
    private static CFGToDotGraph drawer;
    private static final SootConfig sootConfig = new SootConfig();

    public static void buildControlFlowGraph(String classesPath, String targetClassName, MethodContext entryMethod) {
        if (classesPath != null) {
            sootConfig.setupSoot(targetClassName, true, classesPath);
        } else {
            sootConfig.setupSoot(targetClassName, true);
        }
        dotGraph = new DotGraphWrapper(entryMethod.getMethodName());

        //获取指定的方法
        SootMethod targetMethod =  Scene.v().getMethod(entryMethod.getMethodSignature());

        //获取方法的Jimple body
        JimpleBody jimpleBody = (JimpleBody) targetMethod.retrieveActiveBody();
//        生成控制流图
        CompleteUnitGraph cfg = new CompleteUnitGraph(jimpleBody);
//        遍历控制流图
        graphTraverse(cfg);
        dotGraph.plot("cfg",targetClassName,entryMethod.getMethodName());
    }

    /***
     * output the control flow graph of the method
     * @param classPath the path of the class
     * @param classNameForAnalysis the name of the class
     * @param sourceMethodContext the context of the method
     */
    public static void buildPrettyControlFlowGraph(String classPath, String classNameForAnalysis, MethodContext sourceMethodContext) {

        if (classPath != null) {
            sootConfig.setupSoot(classNameForAnalysis, true, classPath);
        } else {
            sootConfig.setupSoot(classNameForAnalysis, true);
        }

        SootMethod srcMethod = Scene.v().getMethod(sourceMethodContext.getMethodSignature());
        JimpleBody jimpleBody = (JimpleBody) srcMethod.retrieveActiveBody();
        getPrettyCFG(srcMethod.getActiveBody(), jimpleBody, classNameForAnalysis, sourceMethodContext.getMethodName());
    }

    private static void graphTraverse(CompleteUnitGraph cfg)
    {
        int nodeId = 0;
        Map<Unit, Integer> nodeIds = new HashMap<>();//创建一个map用于存储basic block节点和节点的id
        for (Unit unit : cfg) {
            List<Unit> successors = cfg.getSuccsOf(unit);//获取当前节点的后继节点
            for (Unit successor : successors) {
                if (!nodeIds.containsKey(unit)) {//如果当前节点不在map中，将当前节点加入map
                    nodeIds.put(unit, nodeId++);
                }
                if (!nodeIds.containsKey(successor)) {//如果后继节点不在map中，将后继节点加入map
                    nodeIds.put(successor, nodeId++);
                }
                dotGraph.drawEdge(String.valueOf(nodeIds.get(unit)), String.valueOf(nodeIds.get(successor)));//连接当前节点和后继节点
            }
        }
        for (Map.Entry<Unit, Integer> entry : nodeIds.entrySet()) {
            Unit unit = entry.getKey();
            Integer id = entry.getValue();
            DotGraphNode node = dotGraph.getNode(id.toString());
            node.setLabel(unit.toString());
            if (unit instanceof ReturnStmt) {
                node.setAttribute("style", "filled");
                node.setAttribute("fillcolor", "lightgray");
            } else if (unit.equals(cfg.getHeads().get(0))) {
                node.setAttribute("style", "filled");
                node.setAttribute("fillcolor", "gray");
            }
        }
    }
    /***
     * output the control flow graph of the method
     * @param body the body of the method
     * @param jimpleBody the Jimple body of the method
     * @param ClassName the name of the class
     * @param entryMethod the name of the method
     * In this method,wo only wanna beautiful control flow graph,but not disturb the attention of the LLM
     */
    private static void getPrettyCFG(Body body, JimpleBody jimpleBody, String ClassName, String entryMethod) {
        drawer = new CFGToDotGraph();
        drawer.setBriefLabels(false);//标签简洁模式
        drawer.setOnePage(false);//允许多页
        drawer.setUnexceptionalControlFlowAttr("color", "black");//正常控制流颜色
        drawer.setExceptionalControlFlowAttr("color", "red");//异常控制流颜色
        drawer.setExceptionEdgeAttr("color", "lightgray");//异常边颜色
        drawer.setShowExceptions(true);//显示异常流向


        CFGGraphType cfgGraphType = CFGGraphType.getGraphType("ClassicCompleteUnitGraph");
        DirectedGraph<?> graph = cfgGraphType.buildGraph(body);

        DotGraph dotGraphReal = cfgGraphType.drawGraph(drawer, graph, jimpleBody);

        //生成dot文件
        String dotFilePath = "./sootOutput/dot/cfg/" + ClassName + "." + entryMethod + ".dot";
        //check the dotFilePath contains illegal characters
        dotFilePath = dotFilePath.replace("<", "").replace(">", "").replace(":", "");
        folderExistenceTest(dotFilePath);
        dotGraphReal.plot(dotFilePath);

        //生成png文件
        String pngFilePath = "./sootOutput/pic/cfg/" + ClassName + "." + entryMethod + ".png";
        //check the pngFilePath contains illegal characters
        pngFilePath = pngFilePath.replace("<", "").replace(">", "").replace(":", "");
        DotGraphWrapper.convertDotToPng(dotFilePath, pngFilePath);
    }

    public static void main(String[] args) {
        String targetClassName = "TestCaseDroid.test.Test4";
        String entryMethod = "<TestCaseDroid.test.Test4: void <init>()>";
        MethodContext methodEntryContext = new MethodContext(entryMethod);
        buildControlFlowGraph(null, targetClassName, methodEntryContext);
    }
}
