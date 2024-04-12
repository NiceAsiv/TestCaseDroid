package TestCaseDroid;

import TestCaseDroid.graph.BuildCallGraphForJar;
import TestCaseDroid.graph.BuildControlFlowGraph;
import TestCaseDroid.graph.BuildICFG;
import TestCaseDroid.utils.FileUtils;
import org.apache.commons.cli.*;

public class TestCaseDroidApplication {
    public static void main(String[] args) {
        Options options = getOptions();
        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;
        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("TestCaseDroid", options);
            System.exit(1);
            return;
        }

        String processPath = cmd.getOptionValue("path");
        String graphType = cmd.getOptionValue("graph");
        String classNameForAnalysis = cmd.getOptionValue("class");
        String methodNameForAnalysis = cmd.getOptionValue("method");

        //check if the process path exists
        if(!FileUtils.isPathExist(processPath)) {
            System.out.println("Error: The path does not exist.");
            formatter.printHelp("usage: TestCaseDroid", options, true);
        }
        if (graphType.equals("cg")) {
            BuildCallGraphForJar.buildCallGraphForJar(processPath,classNameForAnalysis,methodNameForAnalysis);
        } else if (graphType.equals("cfg")) {
            BuildControlFlowGraph.buildControlFlowGraphForClass(processPath,classNameForAnalysis,methodNameForAnalysis);
        } else if (graphType.equals("icfg")) {
            BuildICFG.buildICFGForClass(processPath,classNameForAnalysis,methodNameForAnalysis);
        } else {
            System.out.println("Error: Invalid graph type. Use 'cg', 'cfg', or 'icfg'.");
            formatter.printHelp("usage: TestCaseDroid", options, true);
        }
        // Now you can use these values to perform the required operations
    }

    /**
     * Get the command line options
     * @return Options
     */
    private static Options getOptions() {
        Options options = new Options();
        Option help = new Option("h", "help", false, "display help");
        options.addOption(help);

        Option path = new Option("p", "path", true, "select jar path or class file path");
        path.setRequired(true);
        options.addOption(path);

        Option graph = new Option("g", "graph", true, "select graph type");
        graph.setRequired(true);
        options.addOption(graph);

        Option className = new Option("c", "class", true, "select class name for analysis");
        className.setRequired(false);
        options.addOption(className);

        Option methodName = new Option("m", "method", true, "select method name for analysis");
        methodName.setRequired(false);
        options.addOption(methodName);
        return options;
    }
}