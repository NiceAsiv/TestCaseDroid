package TestCaseDroid.utils;

import lombok.extern.slf4j.Slf4j;
import soot.SootMethod;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
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
    public static String classPathParser(String ...classPath){
        if (classPath.length>1){
            log.error("Only one class path is allowed");
            throw new RuntimeException("Only one class path is allowed");
        }else if (classPath.length==1){
            String path = classPath[0];
            if (isAbsolutePath(path)){
                return path;
            }else {
                return getAbsolutePath(path);
            }
        }
        return null;
    }

    //相对路径转绝对路径
    public static String getAbsolutePath(String relativePath){
        return System.getProperty("user.dir") + File.separator + relativePath;
    }

    public static boolean isAbsolutePath(String pathStr) {
        Path path = Paths.get(pathStr);
        return path.isAbsolute();
    }
}
