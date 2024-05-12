package TestCaseDroid.analysis.info;

import lombok.Getter;
import lombok.Setter;
import TestCaseDroid.config.SootConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import soot.Scene;
import soot.SootClass;
import soot.SootMethod;

/**
 * 函数签名模糊搜索
 */
public class SignatureSearch {
    @Getter
    @Setter
    private String className;// 必要，模糊搜索的类名
    private String methodName;// 必要，模糊搜索的方法名

    public SignatureSearch(String entryClassName, String methodName) {
        this.className = entryClassName;
        this.methodName = methodName;
        SootConfig sootConfig = new SootConfig();
        sootConfig.setupSoot(className, false);
    }

    public void getMethodSignature() {
        SootClass sootClass = Scene.v().getSootClass(className);
        sootClass.setApplicationClass();
        List<SootMethod> methods = sootClass.getMethods();
        List<SootMethod> result = new ArrayList<>();
        // 搜索匹配的方法需要考虑重载的情况
        for (SootMethod method : methods) {
            if (method.getName().equals(this.methodName)) {
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
        Scanner scanner = new Scanner(System.in);
        System.out.println("Please input the class name:");
        String className = scanner.nextLine();
        System.out.println("Please input the method name:");
        String methodName = scanner.nextLine();
        SignatureSearch signatureSearch = new SignatureSearch(className, methodName);
        signatureSearch.getMethodSignature();
    }

}
