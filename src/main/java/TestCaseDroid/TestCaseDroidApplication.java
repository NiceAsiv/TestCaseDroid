package TestCaseDroid;

import TestCaseDroid.analysis.ClassInfoExtractor;
import TestCaseDroid.analysis.reachability.*;
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

        String classPath = cmd.getOptionValue("path");
        String graphType = cmd.getOptionValue("graph");
        String classNameForAnalysis = cmd.getOptionValue("entryClass");
        String sourceMethodSig = cmd.getOptionValue("sourceMethodSig");
        String targetMethodSig = cmd.getOptionValue("targetMethodSig");
        String reachabilityType = cmd.getOptionValue("reachability");
        String extraInfo = cmd.getOptionValue("extra");

        //check if the process path exists
        if(classPath==null || !FileUtils.isPathExist(classPath)) {
            System.out.println("Error: The path does not exist.");
            formatter.printHelp("usage: TestCaseDroid", options, true);
        }


        if (reachabilityType != null) {
            //check if the source method and target method is not null
            if (sourceMethodSig == null || targetMethodSig == null) {
                System.out.println("Error: The source method or target method is not specified.");
                formatter.printHelp("usage: TestCaseDroid", options, true);
            } else {
                MethodContext sourceMethodContext = new MethodContext(sourceMethodSig);
                MethodContext targetMethodContext = new MethodContext(targetMethodSig);
                switch (reachabilityType) {
                    case "cg":
                        ReachabilityCG reachabilityCG = new ReachabilityCG(classNameForAnalysis, targetMethodSig, sourceMethodSig,classPath);
                        reachabilityCG.runAnalysis();
                        break;
                    case "icfg":
                        ReachabilityICFG reachabilityICFG = new ReachabilityICFG(classNameForAnalysis, classPath);
                        reachabilityICFG.runAnalysis(sourceMethodContext, targetMethodContext);
                        break;
                    case "cfg":
                        ReachabilityCFG reachabilityCFG = new ReachabilityCFG(classNameForAnalysis, sourceMethodContext.getMethodName(), classPath);
                        reachabilityCFG.runAnalysis(targetMethodContext);
                        break;
                    case "bicfg":
                        BackwardReachabilityICFG backwardReachabilityICFG = new BackwardReachabilityICFG(classNameForAnalysis, classPath);
                        backwardReachabilityICFG.runAnalysis(sourceMethodContext, targetMethodContext);
                        break;

                    default:
                        System.out.println("Error: Invalid reachability analysis type. Use 'cg' 'cfg' or 'icfg'.");
                        formatter.printHelp("usage: TestCaseDroid", options, true);
                        break;
                }
            }
        }

        if (graphType !=null)
        {
            if (sourceMethodSig == null) {
                System.out.println("Error: The source method is not specified.");
                formatter.printHelp("usage: TestCaseDroid", options, true);
            }else {
                MethodContext sourceMethodContext = new MethodContext(sourceMethodSig);
                switch (graphType) {
                    case "cg":
                        BuildCallGraphForJar.buildCallGraphForJar(classPath, classNameForAnalysis, sourceMethodContext);
                        break;
                    case "cfg":
                        BuildControlFlowGraph.buildControlFlowGraph(classPath, classNameForAnalysis,sourceMethodContext);
                        break;
                    case "icfg":
                        BuildICFG.buildICFGForClass(classPath, classNameForAnalysis, sourceMethodContext);
                        break;
                    default:
                        System.out.println("Error: Invalid graph type. Use 'cg', 'cfg','icfg', or 'bicfg'.");
                        formatter.printHelp("usage: TestCaseDroid", options, true);
                        break;
                }
            }
        }
        if (extraInfo!=null)
        {
            if (extraInfo.equals("true"))
            {
                ClassInfoExtractor.runAnalysis(classNameForAnalysis, classPath);
            }
        }

    }

    /**
     * Get the command line options
     * @return Options
     */
    private static Options getOptions() {
        Options options = new Options();
        //帮助选项
        Option help = new Option("h", "help", false, "display help");
        options.addOption(help);

        //class path选项 要分析的jar包路径或者class文件路径(对于maven项目，可以需要指定至target/classes目录)
        Option path = new Option("p", "path", true, "select jar path or class file path, e.g.,-p /path/target/classes");
        path.setRequired(true);
        options.addOption(path);

        //输入要分析的类名
        Option entryClass = new Option("ec", "entryClass", true, "entry class for analysis e.g., -ec TestCaseDroid.test.CallGraphs");
        entryClass.setRequired(true);
        options.addOption(entryClass);


        Option entryMethodSig = new Option("sms", "sourceMethodSig", true, "entry source method signature for analysis or graph build e.g., -sms <TestCaseDroid.test.CallGraphs: void main(java.lang.String[])>");
        entryMethodSig.setRequired(false);
        options.addOption(entryMethodSig);

        Option targetMethodSig = new Option("tms", "targetMethodSig", true, "target method signature for analysis e.g., -tms <TestCaseDroid.test.CallGraphs: void main(java.lang.String[])>");
        targetMethodSig.setRequired(false);
        options.addOption(targetMethodSig);

        //graph选项 选择分析的图类型

        Option graph = new Option("g", "graph", true, "select graph type");
        graph.setRequired(false);
        options.addOption(graph);

        //可达性分析
        Option reachability = new Option("r", "reachability", true, "select reachability analysis type, including 'cg', 'cfg', 'icfg', 'bicfg'(backward icfg) e.g., -r bicfg");
        reachability.setRequired(false);
        options.addOption(reachability);


        Option extraInfo = new Option("e", "extra", true, "select extra information");
        extraInfo.setRequired(false);
        options.addOption(extraInfo);
        return options;
    }
}