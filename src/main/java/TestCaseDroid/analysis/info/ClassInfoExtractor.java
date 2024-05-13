package TestCaseDroid.analysis.info;

import TestCaseDroid.config.SootConfig;
import lombok.extern.slf4j.Slf4j;
import soot.Scene;
import soot.SootClass;
import soot.SootField;
import soot.SootMethod;
import soot.util.Chain;

import java.lang.reflect.Field;
import java.util.List;

@Slf4j
public class ClassInfoExtractor {
    private static ClassInfo extractClassInfo(String className) {
        SootConfig sootConfig = new SootConfig();
        sootConfig.setupSoot(className, false);
        ClassInfo classInfo = new ClassInfo(className);

        SootClass sootClass = Scene.v().getSootClass(className);
        sootClass.setApplicationClass();

        // Extract class name
        classInfo.setClassName(className);
        // Extract package name
        classInfo.setPackageName(className.substring(0, className.lastIndexOf('.')));

        // Extract class fields
        extractFieldsValue(sootClass, classInfo);
        // Extract class methods
        List<SootMethod> methods = sootClass.getMethods();
        for (SootMethod method : methods) {
            try {
                classInfo.addMethod(method.getName(), method.retrieveActiveBody().toString());
                classInfo.addMethodBody(method.getName(), method.retrieveActiveBody().toString());
            } catch (Exception e) {
                log.error("Failed to extract method: " + method.getName());
            }
        }
        return classInfo;
    }

    private static ClassInfo extractClassInfo(String className, String classPath) {
        SootConfig sootConfig = new SootConfig();
        sootConfig.setupSoot(className, false, classPath);
        ClassInfo classInfo = new ClassInfo(className);

        SootClass sootClass = Scene.v().getSootClass(className);
        sootClass.setApplicationClass();

        // Extract class name
        classInfo.setClassName(className);
        // Extract package name
        classInfo.setPackageName(className.substring(0, className.lastIndexOf('.')));

        // Extract class fields
        extractFieldsValue(sootClass, classInfo);
        // Extract class methods
        List<SootMethod> methods = sootClass.getMethods();
        for (SootMethod method : methods) {
            try {
                classInfo.addMethod(method.getName(), method.retrieveActiveBody().toString());
                classInfo.addMethodBody(method.getName(), method.retrieveActiveBody().toString());
            } catch (Exception e) {
                log.error("Failed to extract method: " + method.getName());
            }
        }
        return classInfo;
    }

    /**
     * Extract class fields value from the class 利用反射机制获取类的字段值
     * 
     * @param sootClass SootClass
     * @param classInfo ClassInfo
     */
    private static void extractFieldsValue(SootClass sootClass, ClassInfo classInfo) {
        Chain<SootField> fields = sootClass.getFields();
        Class<?> clazz = null;
        try {
            clazz = Class.forName(sootClass.getName());
        } catch (ClassNotFoundException e) {
            log.error("Failed to load class: {}", sootClass.getName());
        }

        // Extract field value
        for (SootField field : fields) {
            try {
                assert clazz != null;
                Field fieldObj = clazz.getDeclaredField(field.getName());
                fieldObj.setAccessible(true);// 设置为可访问
                Object value = fieldObj.get(clazz.newInstance());
                if (value != null) {
                    classInfo.addField(field.getSignature(), value.toString());
                } else {
                    classInfo.addField(field.getSignature(), "null");
                }
            } catch (IllegalAccessException | NoSuchFieldException e) {
                log.error("Failed to extract field value: {}", field.getName());
            } catch (InstantiationException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void runAnalysis(String className) {
        ClassInfo classInfo = extractClassInfo(className);
        System.out.println(classInfo);
    }

    public static void runAnalysis(String className, String classPath) {
        ClassInfo classInfo = extractClassInfo(className, classPath);
        System.out.println(classInfo);
    }

    public static void main(String[] args) {
        ClassInfoExtractor.runAnalysis("TestCaseDroid.test.CallGraph");
    }
}
