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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static TestCaseDroid.utils.FileUtils.folderExistenceTest;

/**
 * Wrapper for the DotGraph class
 */
@Slf4j
public class DotGraphWrapper {
    private static boolean graphvizWarningShown;
    private final DotGraph dotGraph;
    private String graphName;
    private final Map<String, Set<String>> edgeMap;

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
        edgeMap = new LinkedHashMap<>();
        // this.dotGraph.setGraphAttribute("fontname", "Helvetica");
        // this.dotGraph.setGraphAttribute("fontsize", "12");
    }

    public void drawEdge(String src, String tgt) {
        this.dotGraph.drawEdge(src, tgt);
        edgeMap.computeIfAbsent(src, ignored -> new LinkedHashSet<>()).add(tgt);
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
        for (Map.Entry<String, Set<String>> entry : edgeMap.entrySet()) {
            for (String target : entry.getValue()) {
                newDotGraphWrapper.drawEdge(entry.getKey(), target);
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
        String graphvizPath = graphvizExecutable();
        folderExistenceTest(outputFilePath);
        try {
            Process process = new ProcessBuilder(
                    graphvizPath, "-Tpng", dotFilePath, "-Gdpi=300",
                    "-Gfontname=Arial", "-o", outputFilePath)
                    .redirectErrorStream(true)
                    .start();
            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (output.length() > 0) {
                        output.append(System.lineSeparator());
                    }
                    output.append(line);
                }
            }
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                log.warn("Graphviz exited with code {}. DOT output is still available at {}. {}",
                        exitCode, dotFilePath, output);
            }
        } catch (IOException ex) {
            warnGraphvizUnavailable(dotFilePath);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            log.warn("Graphviz conversion was interrupted. DOT output is available at {}.", dotFilePath);
        }
    }

    private static String graphvizExecutable() {
        String configured = System.getenv("GRAPHVIZ");
        if (configured == null || configured.trim().isEmpty()) {
            return "dot";
        }
        File configuredFile = new File(configured);
        if (configuredFile.isFile()) {
            return configuredFile.getAbsolutePath();
        }
        String executable = System.getProperty("os.name", "")
                .toLowerCase().contains("win") ? "dot.exe" : "dot";
        File inBin = new File(new File(configuredFile, "bin"), executable);
        if (inBin.isFile()) {
            return inBin.getAbsolutePath();
        }
        return new File(configuredFile, executable).getAbsolutePath();
    }

    private static synchronized void warnGraphvizUnavailable(String dotFilePath) {
        if (!graphvizWarningShown) {
            graphvizWarningShown = true;
            log.warn("Graphviz was not found via GRAPHVIZ or PATH; PNG conversion is skipped. "
                    + "DOT output is available at {}.", dotFilePath);
        }
    }

}
