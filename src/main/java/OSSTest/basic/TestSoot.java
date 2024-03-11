package OSSTest.basic;

import soot.*;
import soot.jimple.Stmt;
import soot.options.Options;

import java.io.File;
import java.util.Iterator;
import java.util.Map;

public class TestSoot extends BodyTransformer {
    public static void main(String[] args) {
        String mainclass = "OSSTest.tests.HelloThread";


        //设置Soot的 classpath
        String javapath = System.getProperty("java.class.path");
        System.out.println(javapath);
        String jredir = System.getProperty("java.home")+"/lib/rt.jar";
        String path = javapath+ File.pathSeparator+jredir;
        //如果你不想使用上述查找lib的方式，你还可以
        //Options.v().set_prepend_classpath(true);
        //然后指定你的扫描class包路径比如
        //path="./target/classes/";
        //Options.v().set_soot_classpath(path);

        Scene.v().setSootClassPath(path);


        //增加过程内分析阶段
        TestSoot analysis = new TestSoot();
        PackManager.v().getPack("jtp").add(new Transform("jtp.TestSoot", analysis));

        //加载和设置main class
        Options.v().set_app(true);//设置为应用类 作用是只分析应用类
        SootClass appclass = Scene.v().loadClassAndSupport(mainclass);
        Scene.v().setMainClass(appclass);
        Scene.v().loadNecessaryClasses();

        //run soot
        PackManager.v().runPacks();
    }
    @Override
    protected void internalTransform(Body body, String phaseName, Map<String, String> options) {
        Iterator<Unit> it = body.getUnits().snapshotIterator();
        while (it.hasNext()) {
            Stmt stmt = (Stmt) it.next();
            System.out.println(stmt);
        }
    }


}
