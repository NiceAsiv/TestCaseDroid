package TestCaseDroid.analysis.info;

import TestCaseDroid.config.SootConfig;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 函数签名模糊搜索
 */
public class SignatureSearch {

    private static MethodInfo methodInfo = new MethodInfo();
    private static final Pattern METHOD_SIGNATURE_PATTERN = Pattern.compile("^(.+)#(\\w+)\\((.*)\\)$");
    private static final Pattern METHOD_SIGNATURE_PATTERN_WITHOUT_PARAMS = Pattern.compile("^(.+)#(\\w+)$");

    public static Boolean parseIDEARef(String ideaRef) {
        // IDEA中的引用格式为：
        // TestCaseDroid.test.CFG#method2(int)
        // TestCaseDroid.test.CFG#method2(java.lang.String)
        // TestCaseDroid.test.CFG#method2()
        // TestCaseDroid.test.CFG#method2(int, int)
        Matcher matcher = METHOD_SIGNATURE_PATTERN.matcher(ideaRef);
        if (matcher.find()) {
            methodInfo.isOverRide = true;
            methodInfo.className = matcher.group(1);
            methodInfo.methodName = matcher.group(2);
            if (matcher.group(3).isEmpty()) {
                methodInfo.paramNum = 0;
            } else {
                String[] params = matcher.group(3).split(",");
                methodInfo.paramNum = params.length;
                // 去掉参数前后的空格
                for (int i = 0; i < params.length; i++) {
                    params[i] = params[i].trim();
                }
                methodInfo.paramters.addAll(Arrays.asList(params));
            }
            return true;
        } else {
            // TestCaseDroid.test.CFG#main
            matcher = METHOD_SIGNATURE_PATTERN_WITHOUT_PARAMS.matcher(ideaRef);
            if (matcher.find()) {
                methodInfo.isOverRide = false;
                methodInfo.className = matcher.group(1);
                methodInfo.methodName = matcher.group(2);
                methodInfo.paramNum = 0;
                return true;
            } else {
                return false;
            }
        }
    }

    public SignatureSearch(String entryClassName, String methodName, String classPath) {
        methodInfo = new MethodInfo(entryClassName, methodName);
        SootConfig sootConfig = new SootConfig();
        sootConfig.setupSoot(entryClassName, false, classPath);
    }

    public static String getMethodSignatureByIDEARef(String ideaRef, String classPath) {
        if (!parseIDEARef(ideaRef)) {
            return null;
        }
        SootConfig sootConfig = new SootConfig();
        sootConfig.setupSoot(methodInfo.className, false, classPath);

        SootClass sootClass = Scene.v().getSootClass(methodInfo.className);
        sootClass.setApplicationClass();
        if (!methodInfo.isOverRide) {
            SootMethod method = sootClass.getMethodByName(methodInfo.methodName);
            return method.getSignature();
        } else {
            for (SootMethod method : sootClass.getMethods()) {
                if (method.getName().equals(methodInfo.methodName)
                        && method.getParameterCount() == methodInfo.paramNum) {
                    if (methodInfo.paramNum == 0) {
                        return method.getSignature();
                    }
                    List<Type> methodParams = method.getParameterTypes();
                    boolean match = true;
                    for (int i = 0; i < methodParams.size(); i++) {
                        if (!methodParams.get(i).toString().equals(methodInfo.paramters.get(i))) {
                            match = false;
                            break;
                        }
                    }
                    if (match) {
                        return method.getSignature();
                    }
                }
            }
        }
        return null;
    }

    public void getMethodSignature() {
        SootClass sootClass = Scene.v().getSootClass(methodInfo.className);
        sootClass.setApplicationClass();
        List<SootMethod> methods = sootClass.getMethods();
        List<SootMethod> result = new ArrayList<>();
        // 搜索匹配的方法需要考虑重载的情况
        for (SootMethod method : methods) {
            if (method.getName().equals(methodInfo.methodName)) {
                result.add(method);
            }
        }
        if (result.isEmpty()) {
            System.out.println("No method found, please check the class name and method name.");
        } else {
            System.out.println("Found " + result.size() + " methods:");
            for (SootMethod method : result) {
                System.out.println(method.getSignature());
            }
        }
    }

    public static void main(String[] args) {
        getMethodSignatureByIDEARef("TestCaseDroid.test.CFG#method2(int, int)", "E:\\Tutorial\\TestCaseDroid\\target\\classes");
    }

}
