package TestCaseDroid.utils;

import soot.SootMethod;
import soot.Unit;
import soot.toolkits.graph.UnitGraph;
import soot.util.dot.DotGraph;

import java.io.File;
import java.util.ArrayList;


public class SootUtils {
    public  static  ArrayList<String> excludeClassesList = addExcludeClassesList();

    /**
     * Convert a dot file to a png file
     * @param dotFilePath the dot file path
     * @param outputFilePath the output png file path
     */
    public static void convertDotToPng(String dotFilePath, String outputFilePath) {
        try {
            String graphvizFilePath = System.getenv("GRAPHVIZ");
            String graphvizPath;
            if (graphvizFilePath == null) {
                throw new RuntimeException("\nPlease set the installation folder for graphviz as an environment variable and name it \"GRAPHVIZ\".\n" +
                        "The graphviz folder is like this: \"D:\\APPdata\\Graphviz-10.0.1-win64\".\n" +
                        "You can download graphviz at https://graphviz.org/download/.\n" +
                        "When you finish that, please restart your IDE.\n");
            } else {
                graphvizPath = graphvizFilePath + File.separator + "bin" + File.separator + "dot.exe";
            }
            //String dotPath = "D:\\APPdata\\Graphviz\\bin\\dot.exe"; //Graphviz software installed location

            // Check if pic output folder exist
            File folder = new File(outputFilePath.substring(0, outputFilePath.lastIndexOf("/")));
            if (!folder.exists()) {
                if (folder.mkdirs()) {
                    System.out.println("Create pic output folder：" + folder.getAbsolutePath());
                } else {
                    System.err.println("Unable to create pic output folder：" + folder.getAbsolutePath());
                }
            } else {
                System.out.println("Pic output folder exist in：" + folder.getAbsolutePath());
            }

            String[] cmd = new String[]{graphvizPath, "-Tpng", dotFilePath, "-o", outputFilePath};
            Runtime rt = Runtime.getRuntime();
            rt.exec(cmd);
        } catch (Exception ex) {
            System.err.println("\nError: " + ex.getMessage());
        }
    }


    /**
     * Check if a method is excluded
     * @param method the method to check
     * @return true if the method is excluded, false otherwise
     */
    public static boolean isNotExcludedMethod(SootMethod method)
    {
        String declaringClassName = method.getDeclaringClass().getName();
        for(String s : excludeClassesList)
        {
            if(declaringClassName.startsWith(s)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get the list of excluded classes
     * @return the list of excluded classes
     */
    protected static ArrayList<String> addExcludeClassesList() {
        if (excludeClassesList == null) {
            excludeClassesList = new ArrayList<String>();
            //排除特定的类
            excludeClassesList.add("java.");
            excludeClassesList.add("sun.");
            excludeClassesList.add("com.sun.");
            excludeClassesList.add("javax.");
            excludeClassesList.add("jdk.");
        }
        return excludeClassesList;
    }

    public static void UnitGraphToDot(UnitGraph graph, String dotFilePath,String graphName) {
        DotGraph dot = new DotGraph(graphName);

        //create node for each unit
        for (Unit u : graph) {
            String unitStr = u.toString();
            dot.drawNode(unitStr).setLabel(unitStr.substring(0, Math.min(unitStr.length(),60))); // set label and shorten the string if it is too long
        }

        //create edge for each edge
        for (Unit u : graph) {
            for (Unit s : graph.getSuccsOf(u)) {
                dot.drawEdge(u.toString(), s.toString());
            }
        }
        dot.plot(dotFilePath);


//        StringBuilder dot = new StringBuilder("digraph G {\n");
//        for (Unit u : graph.getHeads()) {
//            dot.append("entry -> \"").append(u).append("\";\n");
//        }
//        for (Unit u : graph.getTails()) {
//            dot.append("\"").append(u).append("\" -> exit;\n");
//        }
//        for (Unit u : graph) {
//            for (Unit s : graph.getSuccsOf(u)) {
//                dot.append("\"").append(u).append("\" -> \"").append(s).append("\";\n");
//            }
//        }
//        dot.append("}");
//        try {
//            File file = new File(dotFilePath);
//            if (!file.exists()) {
//                file.createNewFile();
//            }
//            java.io.FileWriter fileWriter = new java.io.FileWriter(file);
//            fileWriter.write(dot.toString());
//            fileWriter.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }
}
