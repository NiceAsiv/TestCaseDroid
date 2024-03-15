package TestCaseDroid.config;

import soot.*;
import soot.options.Options;

import java.io.File;

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
    private  static  final String  javaPath = System.getProperty("java.class.path");
    private  static  final String  jreDir = System.getProperty("java.home")+"/lib/rt.jar";
    public  static  final String  sootClassPath = javaPath + File.pathSeparator +  jreDir;
    private String callGraphAlgorithm = "Spark";

    public void setCallGraphAlgorithm(String callGraphAlgorithm) {
        this.callGraphAlgorithm = callGraphAlgorithm;
    }

    public String getCallGraphAlgorithm() {
        return callGraphAlgorithm;
    }


    /**
     * Soot configuration
     * @param ClassName the main class name e.g. "TestCaseDroid.tests.CallGraph"
     * @param constructCallGraph whether to construct call graph
     */
    public void setupSoot(String ClassName, Boolean constructCallGraph)
    {
        //清除soot之前留下的所有缓存
        G.reset();
        //设置Soot类路径
        Options.v().set_soot_classpath(sootClassPath);
//        Scene.v().setSootClassPath(sootClassPath);
        //全程序分析
        Options.v().set_whole_program(true);
        //设将类路径中的类均设为应用类，并仅分析应用类
//        Options.v().set_app(true);
        //排除JDK和其他库
        excludeJDKLibrary();
        //加载必要类
        SootClass appClass = Scene.v().loadClassAndSupport(ClassName);
        //设置主类
        Scene.v().setMainClass(appClass);
        //将待分析类设为应用类
        appClass.setApplicationClass();
        //加载 Soot 依赖的类和命令行指定的类
        Scene.v().loadNecessaryClasses();

        //set to keep line number
        Options.v().set_keep_line_number(true);
        //设置输出格式
        Options.v().set_output_format(Options.output_format_jimple);
        //设置展示详细信息
        Options.v().set_verbose(true);

        //设置保持原变量名
        Options.v().setPhaseOption("jb","use-original-names:true");
//        Options.v().setPhaseOption("jb.dae","only-stack-locals:true"); // 不去优化b = $stack5;的语句，保持原汁原味
//        Options.v().setPhaseOption("jb.cp", "enabled:false");
//        Options.v().setPhaseOption("jb.ls","enabled:false");
//        Options.v().setPhaseOption("jb.dae","enabled:false");
//        Options.v().setPhaseOption("jb.ulp","unsplit-original-locals:false");
//        Options.v().setPhaseOption("jb.a","enabled:false");
//        Options.v().setPhaseOption("jb.cp","enabled:false");



//        PackManager.v().runPacks();//run soot

        //构建控制流图选项，默认是SPARK
        if (constructCallGraph) {
          switch (new SootConfig().callGraphAlgorithm) {
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
                throw new RuntimeException("Unknown call graph algorithm: " + new SootConfig().callGraphAlgorithm);
          }
        }

    }

    private static void excludeJDKLibrary()
    {
        //exclude jdk classes
        Options.v().set_exclude(excludeClassesList);
        //this option must be disabled for a sound call graph
        Options.v().set_no_bodies_for_excluded(true);
        Options.v().set_allow_phantom_refs(true);
    }


}
