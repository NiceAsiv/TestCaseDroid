package TestCaseDroid.config;

import soot.*;
import soot.options.Options;
import soot.util.Chain;

import java.io.File;
import java.util.Collections;
import java.util.Set;

import static TestCaseDroid.utils.SootUtils.excludeClassesList;


/**
 * Soot configuration
 */
public class SootConfig {
    /**
     * javaPath collects all dependency libraries in the project.
     * jreDir is path to rt.jar
     * sootClassPath combines javaPath and jreDir to form the analysis environment.
     */

    // javaPath 收集项目中所有依赖库包括项目自身target目录下的类
    private  static  final String  javaPath = System.getProperty("java.class.path");
    private  static  final String  jreDir = System.getProperty("java.home")+"/lib/rt.jar";
    public  static String  sootClassPath = javaPath + File.pathSeparator +  jreDir;
    private String callGraphAlgorithm = "Spark";


    public void setCallGraphAlgorithm(String callGraphAlgorithm) {
        this.callGraphAlgorithm = callGraphAlgorithm;
    }

    public String getCallGraphAlgorithm() {
        return callGraphAlgorithm;
    }


    /**
     * Soot configuration for jar file
     * @param jarPath the path to the jar file
     * @param constructCallGraph whether to construct call graph
     */
    public void setupSootForJar(String jarPath,Boolean constructCallGraph) {
        //清除soot之前留下的所有缓存
        G.reset();
        jarPath = System.getProperty("user.dir") + File.separator + jarPath;
        sootClassPath= sootClassPath + File.pathSeparator + jarPath;
        //设置Soot类路径
        Options.v().set_soot_classpath(sootClassPath);
        Options.v().set_whole_program(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_process_dir(Collections.singletonList(jarPath));
        Scene.v().loadNecessaryClasses();
        Scene.v().loadBasicClasses();
        commonSetup(constructCallGraph);
    }

    /**
     * Soot configuration for class document
     * @param ClassName the main class name e.g. "TestCaseDroid.tests.CallGraph"
     * @param constructCallGraph whether to construct call graph
     */
    public void setupSoot(String ClassName, Boolean constructCallGraph)
    {
        //清除soot之前留下的所有缓存
        G.reset();
        //设置Soot类路径
        Options.v().set_soot_classpath(sootClassPath);
        //设置是否分析整个程序
        Options.v().set_whole_program(true);
        //设将类路径中的类均设为应用类，并仅分析应用类
//        Options.v().set_app(true)
        //允许phantom引用
        Options.v().set_allow_phantom_refs(true);
        //排除JDK和其他库
        excludeJDKLibrary();
        //加载必要类
        SootClass appClass = Scene.v().loadClassAndSupport(ClassName);
//        //设置主类
//        Scene.v().setMainClass(appClass);
        //将待分析类设为应用类
        appClass.setApplicationClass();
        //加载 Soot 依赖的类和命令行指定的类
        Scene.v().loadNecessaryClasses();

        commonSetup(constructCallGraph);
    }

    /**
     * Common setup for Soot
     * @param constructCallGraph whether to construct call graph
     */
    private void commonSetup(Boolean constructCallGraph) {
        Options.v().set_keep_line_number(true);
        Options.v().set_output_format(Options.output_format_jimple);
        Options.v().set_verbose(true);
        Options.v().setPhaseOption("jb","use-original-names:true");


        if (constructCallGraph) {
            switch (new SootConfig().getCallGraphAlgorithm()) {
                case "CHA":
                    Options.v().setPhaseOption("cg.cha", "on");
                    break;
                case "Spark":
                    Options.v().setPhaseOption("cg.spark", "on");
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
                    throw new RuntimeException("Unknown call graph algorithm: " + new SootConfig().getCallGraphAlgorithm());
            }
        }
//        getBasicInfo();
    }

    public static void getBasicInfo() {
        //获取含有main方法的类
        SootClass mainClass = Scene.v().getMainClass();
        System.out.println("mainClass: " + mainClass.getName());

        //获取main方法
        SootMethod mainMethod = Scene.v().getMainMethod();
        System.out.println("mainMethod: " + mainMethod.getName());

        //获取运行时类 应用类 基础类 所有类
        Chain<SootClass> libraryClasses = Scene.v().getLibraryClasses();
        Chain<SootClass> applicationClasses = Scene.v().getApplicationClasses();
        Set<String> basicClasses = Scene.v().getBasicClasses();
        Chain<SootClass> classes = Scene.v().getClasses();



        //获取当前soot的分析路径：通常为classpath+app-path
        String sootClassPath = Scene.v().getSootClassPath();
        System.out.println("sootClassPath: " + sootClassPath);

        //获取默认JVMclasspath的路径
        String s = Scene.v().defaultClassPath();//rt.jar path
        System.out.println("defaultClassPath: " + s);
    }

    private static void excludeJDKLibrary()
    {
        //exclude jdk classes
        Options.v().set_exclude(excludeClassesList);
        //this option must be disabled for a sound call graph
        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_allow_phantom_refs(true);//关键!!!!
    }

}
