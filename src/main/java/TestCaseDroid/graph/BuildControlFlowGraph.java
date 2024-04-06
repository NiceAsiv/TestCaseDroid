package TestCaseDroid.graph;

import TestCaseDroid.config.SootConfig;
import TestCaseDroid.utils.DotGraphWrapper;
import TestCaseDroid.utils.SootDataProcessUtils;
import lombok.Setter;
import soot.*;
import soot.jimple.JimpleBody;
import soot.toolkits.graph.ClassicCompleteUnitGraph;
import soot.toolkits.graph.DirectedGraph;
import soot.util.cfgcmd.CFGGraphType;
import soot.util.cfgcmd.CFGToDotGraph;
import soot.util.dot.DotGraph;

import java.util.Map;


@Setter
public class BuildControlFlowGraph {
    private static String targetClassName = "TestCaseDroid.test.FastJsonTest";
    private static String entryMethod = "main";
    private static DotGraphWrapper dotGraph = new DotGraphWrapper("controlFlowGraph");
    private static CFGToDotGraph drawer = new CFGToDotGraph();



    public static void main(String[] args) {
           buildControlFlowGraphForClass();
    }
    public static void buildControlFlowGraphForClass() {
        buildControlFlowGraphForClass(null, targetClassName, entryMethod);
    }

    public static void buildControlFlowGraphForClass(String classesPath, String targetClassName, String entryMethod) {
        //配置soot
        SootConfig sootConfig = new SootConfig();
        sootConfig.setCallGraphAlgorithm("Spark");
        if (classesPath != null) {
            sootConfig.setupSoot(BuildControlFlowGraph.targetClassName, true, classesPath);
        } else {
            sootConfig.setupSoot(BuildControlFlowGraph.targetClassName, true);
        }

        SootClass targetClass = Scene.v().getSootClass(targetClassName);

        //获取指定的方法
        SootMethod targetMethod =  targetClass.getMethodByName(entryMethod);

        //获取方法的Jimple body
        JimpleBody jimpleBody = (JimpleBody) targetMethod.retrieveActiveBody();

        //生成控制流图
        ClassicCompleteUnitGraph cfg = new ClassicCompleteUnitGraph(jimpleBody);

        //遍历控制流图
//        graphTraverse(cfg);
//        dotGraph.plot("cfg",targetClassName,entryMethod);
        getPrettyCFG(jimpleBody, jimpleBody, targetClassName, entryMethod);
    }

    private static void graphTraverse(ClassicCompleteUnitGraph cfg)
    {
        //遍历控制流图
        for(Unit unit : cfg)
        {
            //获取当前单元的后继
            for(Unit successor : cfg.getSuccsOf(unit))
            {
                //添加节点
                dotGraph.drawNode(unit.toString());
                dotGraph.drawNode(successor.toString());
                //添加边
                dotGraph.drawEdge(unit.toString(),successor.toString());
            }
        }
    }



    /***
     * output the control flow graph of the method
     * @param b the body of the method
     * @param jimpleBody the Jimple body of the method
     * @param ClassName the name of the class
     * @param entryMethod the name of the method
     * In this method,wo only wanna beautiful control flow graph,but not disturb the attention of the LLM
     */
    private static void getPrettyCFG(Body b, JimpleBody jimpleBody, String ClassName, String entryMethod) {
        drawer = new CFGToDotGraph();
        drawer.setBriefLabels(false);//标签简洁模式
        drawer.setOnePage(false);//允许多页
        drawer.setUnexceptionalControlFlowAttr("color", "black");//正常控制流颜色
        drawer.setExceptionalControlFlowAttr("color", "red");//异常控制流颜色
        drawer.setExceptionEdgeAttr("color", "lightgray");//异常边颜色
        drawer.setShowExceptions(true);//显示异常流向


        CFGGraphType cfgGraphType = CFGGraphType.getGraphType("ClassicCompleteUnitGraph");
        DirectedGraph<?> graph = cfgGraphType.buildGraph(b);

        DotGraph dotGraphReal = cfgGraphType.drawGraph(drawer, graph, jimpleBody);

        //生成dot文件
        String dotFilePath = "./sootOutput/dot/" + ClassName + "." + entryMethod + ".cfg.dot";
        dotGraphReal.plot(dotFilePath);

        //生成png文件
        String pngFilePath = "./sootOutput/pic/" + ClassName + "." + entryMethod + ".cfg.png";
        DotGraphWrapper.convertDotToPng(dotFilePath, pngFilePath);
    }
}
