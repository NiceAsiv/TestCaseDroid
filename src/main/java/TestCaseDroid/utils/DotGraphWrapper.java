package TestCaseDroid.utils;

import TestCaseDroid.analysis.reachability.Context;
import TestCaseDroid.analysis.reachability.MethodContext;
import lombok.extern.slf4j.Slf4j;
import soot.SootMethod;
import soot.Unit;
import soot.util.dot.DotGraph;
import soot.util.dot.DotGraphEdge;
import soot.util.dot.DotGraphNode;

import java.io.File;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import static TestCaseDroid.utils.FileUtils.folderExistenceTest;

/**
 * Wrapper for the DotGraph class
 */
@Slf4j
public class DotGraphWrapper {
    private final DotGraph dotGraph;
    private String graphName;
    private final Multimap<String, String> edgeMap;

    /**
     * Constructor
     *
     * @param graphName the name of the graph
     */
    public DotGraphWrapper(String graphName) {
        this.dotGraph = new DotGraph(graphName);
        this.graphName = graphName;
        // 设置节点的形状
        this.dotGraph.setNodeShape("box");
        edgeMap = ArrayListMultimap.create();
        // this.dotGraph.setGraphAttribute("fontname", "Helvetica");
        // this.dotGraph.setGraphAttribute("fontsize", "12");
    }

    public void drawEdge(String src, String tgt) {
        this.dotGraph.drawEdge(src, tgt);
        edgeMap.put(src, tgt);
    }

    public void drawNode(String node) {
        this.dotGraph.drawNode(node);
    }

    public void setGraphName(String title) {
        this.graphName = title;
        this.dotGraph.setGraphName(title);
    }

    // 实现copy方法
    public DotGraphWrapper copy() {
        DotGraphWrapper newDotGraphWrapper = new DotGraphWrapper(this.graphName);

        // 遍历EdgeMap，将原图中的节点加入新图
        for (String src : edgeMap.keySet()) {
            for (String target : edgeMap.get(src)) {
                newDotGraphWrapper.drawEdge(src, target);
            }
        }
        return newDotGraphWrapper;
    }

    public void setNodeShape(String shape) {
        this.dotGraph.setNodeShape(shape);
    }

    public DotGraphNode getNode(String name) {
        return this.dotGraph.getNode(name);
    }

    /**
     * Plot the graph
     * 
     * @param graphType    the type of graph including "cg", "cfg",
     *                     "icfg","rcfg"(reachability analysis for CFG)
     * @param targetClass  the target class
     * @param targetMethod the target method (optional) but required for "cfg" and
     *                     "icfg"
     * @see DotGraphWrapper#convertDotToPng(String, String)
     */
    public void plot(String graphType, String targetClass, String... targetMethod) {
        if (dotGraph == null) {
            log.error("DotGraph is null");
            return;
        }
        //check if the targetClass contains illegal characters
        targetClass = targetClass.replace("<", "").replace(">", "").replace(":", "");
        switch (graphType) {
            case "cg":
                String callGraphPath = "./sootOutput/dot/cg/" + targetClass + ".dot";
                String outputPath = "./sootOutput/pic/cg/" + targetClass + ".png";
                folderExistenceTest(callGraphPath);
                this.dotGraph.plot(callGraphPath);
                try {
                    convertDotToPng(callGraphPath, outputPath);
                } catch (Exception e) {
                    log.error("Error in converting dot to png", e);
                }
                break;
            case "cfg":
                //检查是存在非法字符
                targetMethod[0] = targetMethod[0].replace("<", "").replace(">", "").replace(":", "");
                String cfgPath = "./sootOutput/dot/cfg/" + targetClass + "." + targetMethod[0] + ".dot";
                String cfgOutputPath = "./sootOutput/pic/cfg/" + targetClass + "." + targetMethod[0] + ".png";
                folderExistenceTest(cfgPath);
                this.dotGraph.plot(cfgPath);
                try {
                    convertDotToPng(cfgPath, cfgOutputPath);
                } catch (Exception e) {
                    log.error("Error in converting dot to png", e);
                }
                break;
            case "icfg":
                String icfgPath = "./sootOutput/dot/icfg/" + targetClass + ".dot";
                String icfgOutputPath = "./sootOutput/pic/icfg/" + targetClass + ".png";
                folderExistenceTest(icfgPath);
                this.dotGraph.plot(icfgPath);
                try {
                    convertDotToPng(icfgPath, icfgOutputPath);
                } catch (Exception e) {
                    log.error("Error in converting dot to png", e);
                }
                break;
            default:
                log.error("Invalid graph type");
                break;
        }
    }

    public void plot(MethodContext sourceMethodContext, MethodContext targetMethodContext, int pathId) {


        String contextPath = "./sootOutput/dot/reachability/" + sourceMethodContext.getClassName() + "."
                + sourceMethodContext.getMethodName() + "_call_" + targetMethodContext.getClassName() + "."
                + targetMethodContext.getMethodName() + "_" + pathId + ".dot";
        String outputPath = "./sootOutput/pic/reachability/" + sourceMethodContext.getClassName() + "_"
                + sourceMethodContext.getMethodName() + "_call_" + targetMethodContext.getClassName() + "_"
                + targetMethodContext.getMethodName() + "_" + pathId + ".png";
        // Check if the path contains illegal characters
        contextPath = contextPath.replace("<", "").replace(">", "").replace(":", "");
        outputPath = outputPath.replace("<", "").replace(">", "").replace(":", "");
        folderExistenceTest(contextPath);
        this.dotGraph.plot(contextPath);
        try {
            convertDotToPng(contextPath, outputPath);
        } catch (Exception e) {
            log.error("Error in converting dot to png", e);
        }
    }

    public static void contextToDotGraph(Context context, String targetClass, String targetMethod, int pathId) {
        DotGraph dotGraphForContext = new DotGraph("Node call stack");
        dotGraphForContext.setNodeShape("box");// 设置节点的形状
        dotGraphForContext.setGraphAttribute("fontname", "Helvetica");
        dotGraphForContext.setGraphAttribute("fontsize", "12");

        Unit previous = null;
        for (Unit current : context.getReversedCallStack()) {
            if (previous != null) {
                // Draw edge
                dotGraphForContext.drawEdge(previous.toString(), current.toString());
            }
            // Draw node
            dotGraphForContext.drawNode(current.toString());
            previous = current;
        }
        String contextPath = "./sootOutput/dot/reachability/" + targetClass + "." + targetMethod + ".unit." + pathId
                + ".dot";
        String outputPath = "./sootOutput/pic/reachability/" + targetClass + "." + targetMethod + ".unit." + pathId
                + ".png";
        // Check if the path contains illegal characters
        contextPath = contextPath.replace("<", "").replace(">", "").replace(":", "");
        outputPath = outputPath.replace("<", "").replace(">", "").replace(":", "");
        folderExistenceTest(contextPath);
        dotGraphForContext.plot(contextPath);
        try {
            convertDotToPng(contextPath, outputPath);
        } catch (Exception e) {
            log.error("Error in converting dot to png", e);
        }
    }

    public static void methodContextToDotGraph(MethodContext methodContext, MethodContext sourceMethodContext,
            MethodContext targetMethodContext, int pathId) {
        DotGraph dotGraphForContext = getDotGraphFromMethodContext(methodContext);
        String contextPath = "./sootOutput/dot/reachability/" + sourceMethodContext.getClassName() + "."
                + sourceMethodContext.getMethodName() + "_call_" + targetMethodContext.getClassName() + "."
                + targetMethodContext.getMethodName() + "_" + pathId + ".dot";
        String outputPath = "./sootOutput/pic/reachability/" + sourceMethodContext.getClassName() + "_"
                + sourceMethodContext.getMethodName() + "_call_" + targetMethodContext.getClassName() + "_"
                + targetMethodContext.getMethodName() + "_" + pathId + ".png";
        // Check if the path contains illegal characters
        contextPath = contextPath.replace("<", "").replace(">", "").replace(":", "");
        outputPath = outputPath.replace("<", "").replace(">", "").replace(":", "");
        folderExistenceTest(contextPath);
        dotGraphForContext.plot(contextPath);
        try {
            convertDotToPng(contextPath, outputPath);
        } catch (Exception e) {
            log.error("Error in converting dot to png", e);
        }
    }

    private static DotGraph getDotGraphFromMethodContext(MethodContext methodContext) {
        DotGraph dotGraphForContext = new DotGraph("Method call stack");
        dotGraphForContext.setNodeShape("box");// 设置节点的形状
        dotGraphForContext.setGraphAttribute("fontname", "Helvetica");
        dotGraphForContext.setGraphAttribute("fontsize", "12");

        SootMethod previousMethod = null;
        for (SootMethod currentMethod : methodContext.getReverseMethodCallStack()) {
            if (previousMethod != null) {
                // Draw edge
                DotGraphEdge edge = dotGraphForContext.drawEdge(previousMethod.getSignature(),
                        currentMethod.getSignature());
                edge.setLabel("call");
            }
            // Draw node
            dotGraphForContext.drawNode(currentMethod.getSignature());
            previousMethod = currentMethod;
        }
        return dotGraphForContext;
    }

    /**
     * Convert a dot file to a png file
     * 
     * @param dotFilePath    the dot file path
     * @param outputFilePath the output png file path
     */
    public static void convertDotToPng(String dotFilePath, String outputFilePath) {
        try {
            String graphvizFilePath = System.getenv("GRAPHVIZ");
            String graphvizPath = getString(graphvizFilePath);
            // Check if pic output folder exist
            folderExistenceTest(outputFilePath);
            // File folder = new File(outputFilePath.substring(0,
            // outputFilePath.lastIndexOf("/")));
            // if (!folder.exists()) {
            // if (folder.mkdirs()) {
            // System.out.println("Create pic output folder：" + folder.getAbsolutePath());
            // } else {
            // System.err.println("Unable to create pic output folder：" +
            // folder.getAbsolutePath());
            // }
            // } else {
            // System.out.println("Pic output folder exist in：" + folder.getAbsolutePath());
            // }

            String[] cmd = new String[] { graphvizPath, "-Tpng", dotFilePath, "-Gdpi=300", "-Gfontname=Arial", "-o",
                    outputFilePath };
            Runtime rt = Runtime.getRuntime();
            rt.exec(cmd);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }
    }

    private static String getString(String graphvizFilePath) {
        String graphvizPath;
        if (graphvizFilePath == null) {
            throw new RuntimeException(
                    "\nPlease set the installation folder for graphviz as an environment variable and name it \"GRAPHVIZ\".\n"
                            +
                            "The graphviz folder is like this: \"D:\\APPdata\\Graphviz-10.0.1-win64\".\n" +
                            "You can download graphviz at https://graphviz.org/download/.\n" +
                            "When you finish that, please restart your IDE.\n");
        } else {
            graphvizPath = graphvizFilePath + File.separator + "bin" + File.separator + "dot.exe";
        }
        return graphvizPath;
    }

}
