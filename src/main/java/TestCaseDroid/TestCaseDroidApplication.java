package TestCaseDroid;

import TestCaseDroid.graph.BuildCallGraphForJar;
import TestCaseDroid.graph.BuildControlFlowGraph;
import TestCaseDroid.graph.BuildICFG;
import org.apache.commons.cli.*;

/**
 * 功能 -h或者-help 显示帮助信息
 * 功能 -p或者-path或者classes-path 选择jar包路径或者class文件路径
 * 功能 -g 或者-graph 选择图的类型 cg cfg icfg
 * 功能 -c或者-class 选择分析的类名 默认为main类
 * 功能 -m或者-method 选择分析的方法名 默认为main方法
 */
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

        String inputFilePath = cmd.getOptionValue("path");
        String graphType = cmd.getOptionValue("graph");
        String classNameForAnalysis = cmd.getOptionValue("class");
        String methodNameForAnalysis = cmd.getOptionValue("method");

        if (graphType.equals("cg")) {
            BuildCallGraphForJar.buildCallGraphForJar(inputFilePath,classNameForAnalysis,methodNameForAnalysis);
        } else if (graphType.equals("cfg")) {
            BuildControlFlowGraph.buildControlFlowGraphForClass(inputFilePath,classNameForAnalysis,methodNameForAnalysis);
        } else if (graphType.equals("icfg")) {
            BuildICFG.buildICFGForClass(inputFilePath,classNameForAnalysis,methodNameForAnalysis);
        } else {
            System.out.println("Invalid graph type");
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