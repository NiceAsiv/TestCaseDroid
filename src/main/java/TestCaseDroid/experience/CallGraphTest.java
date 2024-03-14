package TestCaseDroid.experience;

import soot.*;
import soot.jimple.JimpleBody;
import soot.jimple.internal.JIfStmt;
import soot.options.Options;
import soot.toolkits.graph.ClassicCompleteUnitGraph;
import soot.toolkits.graph.UnitGraph;

import java.io.File;
import java.util.Collections;


public class CallGraphTest {
    public static String sourceDirectory = System.getProperty("user.dir") + File.separator + "target" + File.separator + "classes" + File.separator + "TestCaseDroid"+ File.separator + "test";
    public static String clsName = "FastJsonTest";
    public static String methodName = "main";
    private  static  final String  javaPath = System.getProperty("java.class.path");


    private  static  final String  jreDir = System.getProperty("java.home")+"/lib/rt.jar";
    public  static  final String  sootClassPath = javaPath + File.pathSeparator +  jreDir;
    public static void setupSoot()
    {
        //清除缓存
        G.reset();
        //设置输入文件格式为class文件
        Options.v().set_src_prec(Options.src_prec_class);
        //设置处理路径
        Options.v().set_process_dir(Collections.singletonList(sourceDirectory));
        //设置soot的classpath
        Options.v().set_soot_classpath(sootClassPath);
        //加载java基本库
        Options.v().set_prepend_classpath(true);
//        Options.v().set_allow_phantom_refs(true);


        //将class设为应用类
        SootClass sc = Scene.v().loadClassAndSupport(clsName);
        sc.setApplicationClass();
        //全程序分析
        Options.v().set_whole_program(true);
//        // 显示详细信息
//        Options.v().set_verbose(true);
        //加载 Soot 依赖的类和命令行指定的类
        Scene.v().loadNecessaryClasses();
//        PackManager.v().runPacks();

    }
    public static void main(String[] args) {

        setupSoot();



    }


}
