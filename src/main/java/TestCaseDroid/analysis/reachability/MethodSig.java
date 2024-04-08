package TestCaseDroid.analysis.reachability;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MethodSig {
    String signature;
    String className;
    String methodName;
    String returnType;
    List<String> paramTypes;

    /**
     * 构造函数 从函数签名中提取类名、函数名、返回类型和参数类型
     *
     * @param signature 函数签名 like  <TestCaseDroid.test.CallGraphs: void main(java.lang.String[])>
     */
    public MethodSig(String signature) {
        this.signature = signature;
        String pattern = "<(.*): (.*?) (.*?)\\((.*?)\\)>"; //正则表达式
        if (!signature.matches(pattern)) {
            throw new IllegalArgumentException("Invalid method signature: " + signature);
        }
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(signature);
        if (m.find()) {
            this.className = m.group(1);
            this.returnType = m.group(2);
            this.methodName = m.group(3);
            this.paramTypes = new ArrayList<>();
            if (!m.group(4).isEmpty()) {
                String[] paramArray = m.group(4).split(",");
                this.paramTypes.addAll(Arrays.asList(paramArray));
            }
        } else {
            throw new IllegalArgumentException("Invalid method signature: " + signature);
        }
    }

    public static void main(String[] args) {
        MethodSig methodSig = new MethodSig("<TestCaseDroid.test.CallGraphs: void main(java.lang.String[],int)>");
        System.out.println(methodSig.className);
        System.out.println(methodSig.methodName);
        System.out.println(methodSig.returnType);
        System.out.println(methodSig.paramTypes);
    }
}
