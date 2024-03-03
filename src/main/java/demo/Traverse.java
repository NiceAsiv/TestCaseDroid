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

public class Traverse {
    public static void main(String[] args) {

        PackManager.v().getPack("wjtp").add(

                new Transform("wjtp.myanalysis", new SceneTransformer() {
                    @Override
                    protected void internalTransform(String arg0, Map<String, String> arg1) {
                        // SootClass c = Scene.v().getMainClass();
                        Chain<SootClass> cs = Scene.v().getApplicationClasses();
                        System.out.println("size = "+cs.size());
                        for(SootClass c : cs){
                            System.out.println(c.getName());
                            List<SootMethod> ms = c.getMethods();
                            Chain<SootField> fs = c.getFields();
    
                            for (SootField f : fs) {
                                System.out.println(f.getDeclaration());
                                System.out.println(f.getType());
                            }
                            for (SootMethod m : ms) {
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
        soot.Main.main(new String[] { 
            "-w",
            "-f", "J", 
            "-p", "wjtp.myanalysis", "enabled:true",
            "-soot-class-path", classpath, 
            "-pp",
            "-process-dir", args[0]+args[1]
        });
        return;
    }
}