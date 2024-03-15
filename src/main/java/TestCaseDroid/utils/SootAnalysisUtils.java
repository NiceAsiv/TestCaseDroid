package TestCaseDroid.utils;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;

import java.util.ArrayList;
import java.util.List;

public class SootAnalysisUtils {
    public static void setEntryPoints(String tgtClass, String... entryMethods){
        //设置入口方法，默认入口方法是psvm，如果没有main方法的话则会出现找不到入口报错
        try {
            List<SootMethod> entryPoints = new ArrayList<>();
            if (entryMethods.length == 1) {
                System.out.printf("Add %d method in %s as entrypoint!\n",entryMethods.length,tgtClass);
            } else {
                System.out.printf("Add %d methods in %s as entrypoint!\n",entryMethods.length,tgtClass);
            }

            //加载目标类
            SootClass sc = Scene.v().loadClass(tgtClass, SootClass.BODIES);

            //设置入口方法
            for (String m:entryMethods){
                SootMethod entryPoint = sc.getMethodByName(m);
                entryPoints.add(entryPoint);
            }
            Scene.v().setEntryPoints(entryPoints);

            //设置文本为绿色
            System.out.print(SootVisualizeUtils.TextColor.GREEN.getCode());
            for (SootMethod m:entryPoints){
                System.out.printf("    %s is set as an entrypoint!%n",m.getSignature());
            }

            //输出当前入口方法信息
            SootInfoUtils.reportSootEntryPointsInfo();

        } catch (RuntimeException e) {
            System.err.println("发生了运行时异常，是否是类名和方法名设置错误？\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void setEntryPoints(String tgtClass,Boolean setAll){
        //重载，将目标方法中所有的方法设为入口类
        try {
            //加载目标类
            SootClass sc = Scene.v().loadClass(tgtClass, SootClass.BODIES);
            if(sc.getMethods().size()==1){
                System.out.printf("Add all method in %s as entrypoint!\n",tgtClass);
            }else {
                System.out.printf("Add all methods in %s as entrypoint!\n",tgtClass);
            }

            //设置入口方法
            if (setAll){
                List<SootMethod> entryPoints = new ArrayList<>(sc.getMethods());
                Scene.v().setEntryPoints(entryPoints);
                //设置文本为绿色
                System.out.print(SootVisualizeUtils.TextColor.GREEN.getCode());
                for (SootMethod m:entryPoints){
                    System.out.printf("    %s is set as an entrypoint!%n",m.getSignature());
                }

                //输出当前入口方法信息
                SootInfoUtils.reportSootEntryPointsInfo();
            }

        } catch (RuntimeException e) {
            System.err.println("发生了运行时异常，是否是类名和方法名设置错误？\n" + e.getMessage());
            e.printStackTrace();
        }
    }
}
