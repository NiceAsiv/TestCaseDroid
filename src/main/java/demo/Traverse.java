package demo;

import soot.Body;
import soot.PackManager;
import soot.Scene;
import soot.SceneTransformer;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.Transform;
import soot.Unit;
import soot.util.Chain;

import java.util.Map;
import java.util.List;

/**
 * Traverse类
 * 这个类的主要功能是遍历应用程序的类、方法和字段。
 * 使用方法：java Traverse <classpath> <directory>
 * 例如：java Traverse ./target/classes tests
 */

public class Traverse {
    public static void main(String[] args) {


        PackManager.v().getPack("wjtp").add(
                //Soot wjtp TransForm
                new Transform("wjtp.myanalysis", new SceneTransformer() {
                    @Override
                    protected void internalTransform(String arg0, Map<String, String> arg1) {
                        // SootClass c = Scene.v().getMainClass();
                        Chain<SootClass> cs = Scene.v().getApplicationClasses();
                        System.out.println("The package size = "+cs.size());
                        for(SootClass c : cs){
                            // 打印类名
                            System.out.println(c.getName());
                            List<SootMethod> ms = c.getMethods();
                            Chain<SootField> fs = c.getFields();

                            for (SootField f : fs) {
                                // 打印字段声明和类型
                                System.out.println(f.getDeclaration());
                                System.out.println(f.getType());
                            }
                            for (SootMethod m : ms) {
                                // 打印方法声明、返回类型和参数类型
                                System.out.println(m.getDeclaration());
                                System.out.println(m.getReturnType());
                                System.out.println(m.getParameterTypes());
                            }
                        }
                    }
                })
        );

        String classpath = args[0];
        System.out.println(classpath);
        // 调用soot.Main的main方法，遍历应用程序的类、方法和字段
        soot.Main.main(new String[] { 
            "-w",
            "-f", "J", 
            "-p", "wjtp.myanalysis", "enabled:true",
            "-soot-class-path", classpath, 
            "-pp",
            "-process-dir", args[0]+args[1]
        });
    }
}