package config;

/**
 * This class is used to configure the Soot environment.
 @see <a href="https://www.sable.mcgill.ca/soot/">Soot</a>
 */

import soot.*;
import soot.options.Options;
import soot.util.Chain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class SootConfig {

    //class path
    public  static  final String  sootClassPath = "./target/classes";
    public String callGraphAlgorithm = "Spark";
    public  static  List<String> excludeClassesList ;


    public void setCallGraphAlgorithm(String callGraphAlgorithm) {
        this.callGraphAlgorithm = callGraphAlgorithm;
    }

    //设置Soot的参数
    public  static void setupSoot(String ClassName, boolean constructCallGraph)
    {
        G.reset();//清空soot之前所有操作遗留下的缓存值
        Options.v().set_prepend_classpath(true); // 设置是否在类路径前加上Soot的类路径
        Options.v().set_allow_phantom_refs(true); // 设置是否允许加载未被解析的类
        Options.v().set_keep_line_number(true); // 设置是否记录源代码的所在行
        Options.v().set_soot_classpath(sootClassPath); // 设置Soot的类路径
        Options.v().set_output_format(Options.output_format_jimple); // 设置输出格式为Jimple
        Options.v().set_process_dir(Collections.singletonList(sootClassPath)); // 设置要处理的目录
        Options.v().set_whole_program(constructCallGraph); //以全局应用的模式运行Soot
        Options.v().set_verbose(true); // 设置是否显示详细信息

        //
        Options.v().setPhaseOption("jb","use-original-names:true"); // 设置Jimple阶段选项

        Scene.v().loadNecessaryClasses(); // load necessary classes
        SootClass sootClass = Scene.v().loadClassAndSupport(ClassName); // load the class
        sootClass.setApplicationClass(); // set the class as application class

        //添加to-exclude选项，排除特定的类
        Options.v().set_exclude(addExcludeClassesList());

        PackManager.v().runPacks();//运行Soot的包



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

    public static void getBasicInfo(){
        // 获取主类
        SootClass mainClass = Scene.v().getMainClass();
        System.out.println("Main class is "+ mainClass);
        // 打印主类的所有方法
        for (SootMethod m: mainClass.getMethods()) {
            System.out.println("Method "+ m);
        }
       //获取运行时类 应用类 基础类 所有类
       Chain<SootClass> libraryClasses = Scene.v().getLibraryClasses();
       Chain<SootClass> applicationClasses = Scene.v().getApplicationClasses();
       Set<String> basicClasses = Scene.v().getBasicClasses();
       Chain<SootClass> classes = Scene.v().getClasses();


    }

    /**
     * 添加to-exclude选项，排除特定的类
     * @return 返回排除的类列表
     */
    public static List<String> addExcludeClassesList() {
        if (excludeClassesList == null) {
            excludeClassesList = new ArrayList<String>();
        }
        //排除特定的类
        excludeClassesList.add("java.");
        excludeClassesList.add("sun.");
        excludeClassesList.add("com.sun.");
        excludeClassesList.add("javax.");

        return excludeClassesList;
    }

}
