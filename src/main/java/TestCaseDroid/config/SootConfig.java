package TestCaseDroid.config;

import TestCaseDroid.utils.FileUtils;
import TestCaseDroid.utils.SootUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import soot.*;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.options.Options;

import java.io.File;
import java.util.Collections;


@Setter
@Getter
@Slf4j
public class SootConfig {
    /**
     * javaPath collects all dependency libraries in the project.
     * jreDir is path to rt.jar
     * sootClassPath combines javaPath and jreDir to form the analysis environment.
     */

    // javaPath 收集项目中所有依赖库包括项目自身target目录下的类
    private  static  String  userDir = System.getProperty("user.dir");
    private  static  final String  javaPath = userDir + File.separator + "target" + File.separator + "classes";
    private  static  final String  jreDir = System.getProperty("java.home")+"/lib/rt.jar";
    private  static String  sootClassPath = javaPath + File.pathSeparator + jreDir;
    private String callGraphAlgorithm = "Spark";

    /**
     * Soot configuration for class document
     * @param className the main class name e.g. "TestCaseDroid.tests.CallGraph"
     * @param constructCallGraph whether to construct call graph
     */
    public  void setupSoot(String className, Boolean constructCallGraph)
    {
        //清除soot之前留下的所有缓存
        G.reset();
        //设置Soot类路径
        Options.v().set_soot_classpath(sootClassPath);
        Options.v().set_process_dir(Collections.singletonList(javaPath));
        //设置是否分析整个程序
        Options.v().set_whole_program(true);
        //设将类路径中的类均设为应用类，并仅分析应用类
//        Options.v().set_app(true)
        //允许phantom引用
        Options.v().set_allow_phantom_refs(true);
        //排除JDK和其他库
        excludeJDKLibrary();
        //加载必要类
        SootClass appClass = Scene.v().loadClassAndSupport(className);
//        //设置主类
//        Scene.v().setMainClass(appClass);
        //将待分析类设为应用类
        appClass.setApplicationClass();
        //加载 Soot 依赖的类和命令行指定的类
        Scene.v().loadNecessaryClasses();

        commonSetup(constructCallGraph);
    }


    /**
     * Soot configuration for jar file
     * @param className the main class name e.g. "TestCaseDroid.tests.CallGraph"
     * @param constructCallGraph whether to construct call graph
     * @param classesPath the path to the classes or jar file
     */
    public  void setupSoot(String className,Boolean constructCallGraph,String classesPath) {
        //清除soot之前留下的所有缓存
        G.reset();
        sootClassPath= sootClassPath + File.pathSeparator + FileUtils.classPathParser(classesPath);
        //设置Soot类路径
        Options.v().set_soot_classpath(sootClassPath);
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_process_dir(Collections.singletonList(classesPath));
        //加载指定的类
        SootClass appClass = Scene.v().loadClassAndSupport(className);
        //将待分析类设为应用类
        appClass.setApplicationClass();
        //加载 Soot 依赖的类和命令行指定的类
        Scene.v().loadNecessaryClasses();
        Scene.v().loadBasicClasses();
        commonSetup(constructCallGraph);
    }

    /**
     * Common setup for Soot
     * @param constructCallGraph whether to construct call graph
     */
    private  void commonSetup(Boolean constructCallGraph) {
        Options.v().set_keep_line_number(true);
        Options.v().set_output_format(Options.output_format_jimple);
        Options.v().set_verbose(true);
        Options.v().setPhaseOption("jb","use-original-names:true");

        if (constructCallGraph) {
            switch (this.callGraphAlgorithm) {
                case "CHA":
                    Options.v().setPhaseOption("cg.cha", "on");
                    CHATransformer.v().transform();
                    break;
                case "Spark":
                    Options.v().setPhaseOption("cg.spark","enabled:true");
                    Options.v().setPhaseOption("cg.spark","verbose:true");
                    Options.v().setPhaseOption("cg.spark","on-fly-cg:true");
                    break;
                case "VTA":
                    Options.v().setPhaseOption("cg.spark", "on");
                    Options.v().setPhaseOption("cg.spark", "vta:true");
                    break;
                case "RTA":
                    Options.v().setPhaseOption("cg.spark", "on");
                    Options.v().setPhaseOption("cg.spark", "rta:true");
                    Options.v().setPhaseOption("cg.spark", "on-fly-cg:false");
                    break;
                default:
                    throw new RuntimeException("Unknown call graph algorithm: " + this.callGraphAlgorithm);
            }
        }
        PackManager.v().runPacks();
    }

    private static void excludeJDKLibrary()
    {
        //exclude jdk classes
        Options.v().set_exclude(SootUtils.excludeClassesList);
        Options.v().set_exclude(Collections.singletonList("java.*"));
        Options.v().set_exclude(Collections.singletonList("sun.*"));
        //this option must be disabled for a sound call graph
        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_allow_phantom_refs(true);//关键!!!!
    }
}
