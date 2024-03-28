package TestCaseDroid.utils;

import lombok.extern.slf4j.Slf4j;
import soot.SootMethod;
import soot.Unit;
import soot.toolkits.graph.UnitGraph;
import soot.util.dot.DotGraph;

import java.io.File;
import java.util.ArrayList;

import static TestCaseDroid.utils.SootDataProcessUtils.folderExistenceTest;

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
}
