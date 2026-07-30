package TestCaseDroid;

import TestCaseDroid.analysis.info.ClassInfoExtractor;
import TestCaseDroid.analysis.info.SignatureSearch;
import TestCaseDroid.analysis.reachability.*;
import TestCaseDroid.analysis.report.CallGraphAnalysisOptions;
import TestCaseDroid.analysis.report.CallGraphAnalyzer;
import TestCaseDroid.analysis.report.CallGraphDetailLevel;
import TestCaseDroid.analysis.report.CallGraphGranularity;
import TestCaseDroid.analysis.report.CallGraphReportExporter;
import TestCaseDroid.analysis.report.CallGraphSnapshot;
import TestCaseDroid.graph.BuildCallGraphForJar;
import TestCaseDroid.graph.BuildControlFlowGraph;
import TestCaseDroid.graph.BuildICFG;
import TestCaseDroid.visualization.CallGraphVisualizer;
import org.apache.commons.cli.*;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

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

        if (cmd.hasOption("help")) {
            formatter.printHelp("TestCaseDroid", options, true);
            return;
        }

        String classPath = cmd.getOptionValue("path");
        String graphType = cmd.getOptionValue("graphType");
        String classNameForAnalysis = cmd.getOptionValue("entryClass");
        String sourceMethodSig = cmd.getOptionValue("sourceMethodSig");
        String targetMethodSig = cmd.getOptionValue("targetMethodSig");
        String reachabilityType = cmd.getOptionValue("reachability");
        String extraInfo = cmd.getOptionValue("classInfo");
        String methodName = cmd.getOptionValue("methodName");
        String callGraphAlgorithm = cmd.getOptionValue("callGraphAlgorithm", "CHA");
        boolean visualize = cmd.hasOption("visualize");
        String reportOutput = cmd.getOptionValue("reportOutput");


        // check if the process path is set
        if (classPath == null) {
            System.out.println("Error: The path is not specified.");
            formatter.printHelp("usage: TestCaseDroid", options, true);
            return;
        }
        if (classNameForAnalysis == null) {
            System.out.println("Error: The entry class is not specified.");
            formatter.printHelp("usage: TestCaseDroid", options, true);
            return;
        }

        if (visualize || reportOutput != null) {
            if (sourceMethodSig == null) {
                System.out.println("Error: --sourceMethodSig is required for visualization and reports.");
                formatter.printHelp("usage: TestCaseDroid", options, true);
                return;
            }
            if (sourceMethodSig.contains("#")) {
                sourceMethodSig = SignatureSearch.getMethodSignatureByIDEARef(
                        sourceMethodSig, classPath);
                if (sourceMethodSig == null) {
                    System.out.println("Error: The source method is not found.");
                    return;
                }
            }
            try {
                CallGraphAnalysisOptions analysisOptions = new CallGraphAnalysisOptions()
                        .setMaxDepth(integerOption(cmd, "maxDepth", 12))
                        .setMaxNodes(integerOption(cmd, "maxNodes", 2000))
                        .setIncludeLibraries(cmd.hasOption("includeLibraries"))
                        .setIncludeConstructors(cmd.hasOption("includeConstructors"));
                CallGraphSnapshot snapshot = CallGraphAnalyzer.analyze(
                        classNameForAnalysis, sourceMethodSig, classPath,
                        callGraphAlgorithm, analysisOptions);
                System.out.println("Call-graph snapshot: " + snapshot.getNodes().size()
                        + " nodes, " + snapshot.getEdges().size() + " edges"
                        + (snapshot.isTruncated() ? " (bounded)" : ""));

                if (reportOutput != null) {
                    Path output = Paths.get(reportOutput);
                    if (cmd.hasOption("detailLevel") || cmd.hasOption("granularity")) {
                        CallGraphDetailLevel level = CallGraphDetailLevel.parse(
                                cmd.getOptionValue("detailLevel", "standard"));
                        CallGraphGranularity graphGranularity = CallGraphGranularity.parse(
                                cmd.getOptionValue("granularity", "method"));
                        CallGraphReportExporter.ExportResult result =
                                CallGraphReportExporter.writeReport(
                                        output, snapshot, graphGranularity, level);
                        System.out.println("JSON report: "
                                + result.getJsonPath().toAbsolutePath());
                        System.out.println("Markdown report: "
                                + result.getMarkdownPath().toAbsolutePath());
                    } else {
                        CallGraphReportExporter.writeAiBundle(output, snapshot);
                        System.out.println("AI report bundle: " + output.toAbsolutePath());
                    }
                }
                if (visualize) {
                    if (GraphicsEnvironment.isHeadless()) {
                        System.out.println("Error: Swing visualization is unavailable "
                                + "in a headless environment. Use --reportOutput instead.");
                    } else {
                        CallGraphVisualizer.open(snapshot);
                    }
                }
            } catch (IllegalArgumentException | IOException exception) {
                System.out.println("Error: " + exception.getMessage());
                return;
            }
        }

        if (reachabilityType != null) {
            // check if the source method and target method is not null
            if (sourceMethodSig == null || targetMethodSig == null) {
                System.out.println("Error: The source method or target method is not specified.");
                formatter.printHelp("usage: TestCaseDroid", options, true);
            } else {
                if (sourceMethodSig.contains("#")) {
                    if (SignatureSearch.getMethodSignatureByIDEARef(sourceMethodSig, classPath) == null) {
                        System.out.println("Error: The source method is not found.");
                        formatter.printHelp("usage: TestCaseDroid", options, true);
                        return;
                    }
                    sourceMethodSig = SignatureSearch.getMethodSignatureByIDEARef(sourceMethodSig, classPath);
                }
                if (targetMethodSig.contains("#")) {
                    if (SignatureSearch.getMethodSignatureByIDEARef(targetMethodSig, classPath) == null) {
                        System.out.println("Error: The target method is not found.");
                        formatter.printHelp("usage: TestCaseDroid", options, true);
                        return;
                    }
                    targetMethodSig = SignatureSearch.getMethodSignatureByIDEARef(targetMethodSig, classPath);
                }

                MethodContext sourceMethodContext = new MethodContext(sourceMethodSig);
                MethodContext targetMethodContext = new MethodContext(targetMethodSig);
                switch (reachabilityType) {
                    case "cg":
                        ReachabilityCG reachabilityCG = new ReachabilityCG(classNameForAnalysis, sourceMethodContext,
                                targetMethodContext, classPath, callGraphAlgorithm);
                        reachabilityCG.runAnalysis();
                        break;
                    case "icfg":
                        ReachabilityICFG reachabilityICFG = new ReachabilityICFG(classNameForAnalysis, classPath);
                        reachabilityICFG.runAnalysis(sourceMethodContext, targetMethodContext);
                        break;
                    case "cfg":
                        ReachabilityCFG reachabilityCFG = new ReachabilityCFG(classNameForAnalysis, sourceMethodContext,
                                targetMethodContext, classPath);
                        reachabilityCFG.runAnalysisUsingMarkNode();
//                        reachabilityCFG.runAnalysis();
                        break;
                    case "bicfg":
                        BackwardReachabilityICFG backwardReachabilityICFG = new BackwardReachabilityICFG(
                                classNameForAnalysis, classPath);
                        backwardReachabilityICFG.runAnalysis(sourceMethodContext, targetMethodContext);
                        break;
                    default:
                        System.out.println("Error: Invalid reachability analysis type. Use 'cg' 'cfg' or 'icfg'.");
                        formatter.printHelp("usage: TestCaseDroid", options, true);
                        break;
                }
            }
        }

        if (graphType != null) {
            if (sourceMethodSig == null) {
                System.out.println("Error: The source method is not specified.");
                formatter.printHelp("usage: TestCaseDroid", options, true);
            } else {
                if (sourceMethodSig.contains("#")) {
                    if (SignatureSearch.getMethodSignatureByIDEARef(sourceMethodSig, classPath) == null) {
                        System.out.println("Error: The source method is not found.");
                        formatter.printHelp("usage: TestCaseDroid", options, true);
                        return;
                    }
                    sourceMethodSig = SignatureSearch.getMethodSignatureByIDEARef(sourceMethodSig, classPath);
                }
                MethodContext sourceMethodContext = new MethodContext(sourceMethodSig);
                switch (graphType) {
                    case "cg":
                        BuildCallGraphForJar.buildCallGraphForJar(
                                classPath, callGraphAlgorithm, classNameForAnalysis, sourceMethodContext);
                        break;
                    case "cfg":
                        BuildControlFlowGraph.buildPrettyControlFlowGraph(classPath, classNameForAnalysis,
                                sourceMethodContext);
                        break;
                    case "rcfg":
                        BuildControlFlowGraph.buildControlFlowGraph(classPath, classNameForAnalysis,
                                sourceMethodContext);
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
        if (cmd.hasOption("methodName")) {
            if (methodName != null && !methodName.isEmpty()) {
                if (methodName.contains("#")) {
                    String signature = SignatureSearch.getMethodSignatureByIDEARef(methodName, classPath);
                    if (signature != null) {
                        System.out.println("The method signature is: " + signature);
                    } else {
                        System.out.println("No method found, please check the method name.");
                        formatter.printHelp("usage: TestCaseDroid", options, true);
                    }
                } else {
                    SignatureSearch signatureSearch = new SignatureSearch(classNameForAnalysis, methodName, classPath);
                    signatureSearch.getMethodSignature();
                }
            }
        }
        if (extraInfo != null) {
            if (extraInfo.equals("true")) {
                ClassInfoExtractor.runAnalysis(classNameForAnalysis, classPath);
            }
        }

    }

    /**
     * Get the command line options
     * 
     * @return Options
     */
    private static Options getOptions() {
        Options options = new Options();
        // 帮助选项
        Option help = new Option("h", "help", false, "display help");
        options.addOption(help);

        // class path选项 要分析的jar包路径或者class文件路径(对于maven项目，可以需要指定至target/classes目录)
        Option path = new Option("p", "path", true, "select jar path or class file path, e.g.,-p /path/target/classes");
        path.setRequired(false);
        options.addOption(path);

        // 输入要分析的类名
        Option entryClass = new Option("ec", "entryClass", true,
                "entry class for analysis e.g., -ec TestCaseDroid.test.CallGraphs");
        entryClass.setRequired(false);
        options.addOption(entryClass);

        Option entryMethodSig = new Option("sms", "sourceMethodSig", true,
                "entry source method signature for analysis or graph build e.g., -sms <TestCaseDroid.test.CallGraphs: void main(java.lang.String[])>"
                        +
                        " or -sms use idea reference like TestCaseDroid.test.CFG#method2(int)");
        entryMethodSig.setRequired(false);
        options.addOption(entryMethodSig);

        Option targetMethodSig = new Option("tms", "targetMethodSig", true,
                "target method signature for analysis e.g., -tms <TestCaseDroid.test.CallGraphs: void main(java.lang.String[])>"
                        +
                        " or -tms use idea reference like TestCaseDroid.test.CFG#main");
        targetMethodSig.setRequired(false);
        options.addOption(targetMethodSig);

        // graph选项 选择分析的图类型

        Option graphType = new Option("gt", "graphType", true,
                "select graph type that you want to build, including 'cg', 'cfg'(pretty),'rcfg'(raw),'icfg' e.g., -gt cfg");
        graphType.setRequired(false);
        options.addOption(graphType);

        // 可达性分析
        Option reachability = new Option("r", "reachability", true,
                "select reachability analysis type, including 'cg', 'cfg', 'icfg', 'bicfg'(backward icfg) e.g., -r bicfg");
        reachability.setRequired(false);
        options.addOption(reachability);

        Option callGraphAlgorithm = new Option("cga", "callGraphAlgorithm", true,
                "call graph algorithm: CHA (default), Spark, VTA, or RTA");
        callGraphAlgorithm.setRequired(false);
        options.addOption(callGraphAlgorithm);

        Option visualize = new Option("viz", "visualize", false,
                "open the Java Swing call-graph explorer");
        options.addOption(visualize);

        Option reportOutput = new Option("ro", "reportOutput", true,
                "write UTF-8 JSON and Markdown AI reports to this directory");
        options.addOption(reportOutput);

        Option detailLevel = new Option("dl", "detailLevel", true,
                "report detail: summary, standard, or detailed");
        options.addOption(detailLevel);

        Option granularity = new Option("gr", "granularity", true,
                "graph granularity: package, class, method, or call-site");
        options.addOption(granularity);

        Option maxDepth = new Option(null, "maxDepth", true,
                "maximum call depth for visualization/report extraction (default: 12)");
        options.addOption(maxDepth);

        Option maxNodes = new Option(null, "maxNodes", true,
                "maximum extracted method nodes (default: 2000)");
        options.addOption(maxNodes);

        Option includeLibraries = new Option(null, "includeLibraries", false,
                "include library methods in the snapshot");
        options.addOption(includeLibraries);

        Option includeConstructors = new Option(null, "includeConstructors", false,
                "include constructors and static initializers in the snapshot");
        options.addOption(includeConstructors);

        // 是否要查找方法签名
        Option searchMethodSig = new Option("mn", "methodName", true,
                "if you want to find method signature by method name, e.g., -mn method2 or -mn TestCaseDroid.test.CFG.method2(int)");
        searchMethodSig.setRequired(false);
        options.addOption(searchMethodSig);

        // 是否要提取类信息
        Option classInfo = new Option("ci", "classInfo", true, "if extract class information, e.g., -ci true");
        classInfo.setRequired(false);
        options.addOption(classInfo);
        return options;

    }

    private static int integerOption(CommandLine commandLine,
                                     String option,
                                     int defaultValue) {
        String value = commandLine.getOptionValue(option);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "--" + option + " must be an integer: " + value);
        }
    }
}
