package TestCaseDroid.utils;

import lombok.extern.slf4j.Slf4j;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class SootUtils {
    public  static  ArrayList<String> excludeClassesList = addExcludeClassesList();


    /**
     * Check if a method is excluded
     * @param method the method to check
     * @return true if the method is excluded, false otherwise
     */
    public static boolean isNotExcludedMethod(SootMethod method)
    {
        String declaringClassName = method.getDeclaringClass().getName();
        for(String s : excludeClassesList)
        {
            if(declaringClassName.startsWith(s)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Get the list of excluded classes
     * @return the list of excluded classes
     */
    protected static ArrayList<String> addExcludeClassesList() {
        if (excludeClassesList == null) {
            excludeClassesList = new ArrayList<>();
            //排除特定的类
            excludeClassesList.add("java.");
            excludeClassesList.add("sun.");
            excludeClassesList.add("com.sun.");
            excludeClassesList.add("javax.");
            excludeClassesList.add("jdk.");
        }
        return excludeClassesList;
    }

    /**
     * set the entry points for the analysis
     * @param tgtClass the target class
     * @param entryMethods the entry methods
     */
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
            System.out.print(VisualizeUtils.TextColor.GREEN.getCode());
            for (SootMethod m:entryPoints){
                System.out.printf("    %s is set as an entrypoint!%n",m.getSignature());
            }

            //输出当前入口方法信息
            reportSootEntryPointsInfo();

        } catch (RuntimeException e) {
            System.err.println("发生了运行时异常，是否是类名和方法名设置错误？\n" + e.getMessage());
            log.error(e.getMessage(), e);
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
                System.out.print(VisualizeUtils.TextColor.GREEN.getCode());
                for (SootMethod m:entryPoints){
                    System.out.printf("    %s is set as an entrypoint!%n",m.getSignature());
                }

                //输出当前入口方法信息
                reportSootEntryPointsInfo();
            }

        } catch (RuntimeException e) {
            System.err.println("发生了运行时异常，是否是类名和方法名设置错误？\n" + e.getMessage());
            log.error(e.getMessage(),e);
        }
    }

    public static Boolean isApplicationClass(String tgtClass){
        // 判断被分析的类是否为应用类，并统计类中的方法数量
        System.out.println("--------------------------------");
        SootClass sc = Scene.v().getSootClass(tgtClass);
        System.out.printf("The target class %s is an %s class, loaded with %d methods! %n",
                sc.getName(), sc.isApplicationClass() ? "Application" : "Library", sc.getMethodCount());
        System.out.println("--------------------------------");
        return sc.isApplicationClass();
    }

    public static void reportSootApplicationClassInfo(){
        //遍历当前分析场景中的应用类，并输出其方法
        int classIndex = 0;
        System.out.println("--------------------------------");
        for(SootClass sc : Scene.v().getApplicationClasses()){
            classIndex++;
            int methodIndex = 0;
            System.out.printf("[%d] The class %s is an application class, loaded with %d methods! Methods are as followed:%n",classIndex,sc.getName(),sc.getMethodCount());
            for(SootMethod m:sc.getMethods()){
                methodIndex++;
                System.out.printf("    Method %d: %s%n",methodIndex,m.getSignature());
            }
        }
        System.out.println("--------------------------------");
    }

    public static void reportSootEntryPointsInfo(){
        //设置文本恢复为黑色
        System.out.print(VisualizeUtils.TextColor.RESET.getCode());
        int index=0;
        System.out.println("--------------------------------");
        if(Scene.v().getEntryPoints().size()==1){
            System.out.println("Current entrypoint is:");
        }else {
            System.out.println("Current entrypoint are:");
        }

        for(SootMethod m:Scene.v().getEntryPoints()){
            index++;
            System.out.printf("[%d] %s\n",index,m.getSignature());
        }
        System.out.println("--------------------------------");
    }
}
