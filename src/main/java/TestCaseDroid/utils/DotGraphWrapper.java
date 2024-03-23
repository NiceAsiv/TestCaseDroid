package TestCaseDroid.utils;

import soot.jimple.parser.node.TCase;
import soot.util.Switch;
import soot.util.dot.DotGraph;

import java.util.ArrayList;
import java.util.List;

import static TestCaseDroid.utils.SootDataProcessUtils.folderExistenceTest;

/**
 * Wrapper for the DotGraph class
 */

public class DotGraphWrapper {
    private DotGraph dotGraph;

    /**
     * Constructor
     *
     * @param graphName the name of the graph
     */
    public DotGraphWrapper(String graphName) {
        this.dotGraph = new DotGraph(graphName);
        //设置节点的形状
        this.dotGraph.setNodeShape("box");
//        this.dotGraph.setGraphAttribute("fontname", "Helvetica");
//        this.dotGraph.setGraphAttribute("fontsize", "12");
        //graphname这个参数有啥用捏

    }

    public void drawEdge(String src, String tgt) {
        this.dotGraph.drawEdge(src, tgt);
    }

    public void drawNode(String node) {
        this.dotGraph.drawNode(node);
    }

    /**
     * Plot the graph
     *
     * @param mainClass the main class
     * @param graphType the type of graph including "cg", "cfg", "icfg"
     */
    public void plot(String mainClass, String graphType) {

        switch (graphType) {
            case "cg":
                String callGraphPath = "./sootOutput/dot/" + mainClass + ".cg.dot";
                String outputPath = "./sootOutput/pic/" + mainClass + ".cg.png";
                folderExistenceTest(callGraphPath);
                this.dotGraph.plot(callGraphPath);
                try {
                    SootUtils.convertDotToPng(callGraphPath, outputPath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case "cfg":
                String cfgPath = "./sootOutput/dot/" + mainClass + ".cfg.dot";
                String cfgOutputPath = "./sootOutput/pic/" + mainClass + ".cfg.png";
                folderExistenceTest(cfgPath);
//                this.dotGraph.plot(cfgPath);
                try {
                    SootUtils.convertDotToPng(cfgPath, cfgOutputPath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;

            case "icfg":
                String icfgPath = "./sootOutput/dot/" + mainClass + ".icfg.dot";
                String icfgOutputPath = "./sootOutput/pic/" + mainClass + ".icfg.png";
                folderExistenceTest(icfgPath);
                this.dotGraph.plot(icfgPath);
                try {
                    SootUtils.convertDotToPng(icfgPath, icfgOutputPath);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            default:
                System.err.println("Invalid graph type");
                break;
        }
    }
}
