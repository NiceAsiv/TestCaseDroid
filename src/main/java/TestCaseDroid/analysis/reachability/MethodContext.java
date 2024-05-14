package TestCaseDroid.analysis.reachability;

import lombok.Getter;
import lombok.Setter;
import soot.SootMethod;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Setter
@Getter
public class MethodContext {
    private String className;
    private String methodName;
    private String returnType;
    private String methodSignature; // current method signature
    private List<String> paramTypes;
    Boolean isBackwardReachability = true;
    private Deque<SootMethod> methodCallStack;

    /**
     * Constructor of MethodContext
     * 
     * @param methodSignature The method signature like
     *                        "<TestCaseDroid.test.CallGraphs: void
     *                        main(java.lang.String[])>"
     */
    public MethodContext(String methodSignature) {
        this.methodSignature = methodSignature;
        parseMethodSignature(methodSignature);
        this.methodCallStack = new LinkedList<>();
    }

    public MethodContext(String methodSignature, Deque<SootMethod> methodCallStack) {
        this.methodSignature = methodSignature;
        parseMethodSignature(methodSignature);
        this.methodCallStack = methodCallStack;
    }

    public MethodContext(String className, String methodName, String returnType, List<String> paramTypes) {
        this.className = className;
        this.methodName = methodName;
        this.returnType = returnType;
        this.paramTypes = new ArrayList<>(paramTypes);
        this.methodCallStack = new LinkedList<>();
    }

    public void setMethodSignature(String methodSignature) {
        this.methodSignature = methodSignature;
        parseMethodSignature(methodSignature);
    }

    // copy
    public MethodContext copy() {
        return new MethodContext(this.methodSignature, new LinkedList<>(this.methodCallStack));
    }

    public Deque<SootMethod> getReverseMethodCallStack() {
        Deque<SootMethod> reverseMethodCallStack = new LinkedList<>();
        for (SootMethod method : methodCallStack) {
            reverseMethodCallStack.addFirst(method);
        }
        return reverseMethodCallStack;
    }

    public String getMethodCallStackString() {
        StringBuilder sb = new StringBuilder();
        if (isBackwardReachability) {
            for (SootMethod method : getReverseMethodCallStack()) {
                sb.append(method.getSignature()).append(" -> ");
            }
        } else {
            for (SootMethod method : methodCallStack) {
                sb.append(method.getSignature()).append(" -> ");
            }
        }
        if (sb.length() == 0) {
            return "";
        }
        sb.delete(sb.length() - 4, sb.length());
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(className, methodName, returnType, paramTypes, methodCallStack);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        MethodContext that = (MethodContext) obj;
        return Objects.equals(className, that.className) &&
                Objects.equals(methodName, that.methodName) &&
                Objects.equals(returnType, that.returnType) &&
                Objects.equals(paramTypes, that.paramTypes) && Objects.equals(methodCallStack, that.methodCallStack);
    }

    private void parseMethodSignature(String methodSignature) {
        // if null
        if (methodSignature == null) {
            throw new IllegalArgumentException("Invalid method methodSignature null");
        }

        // 匹配类名、返回值、方法名、参数
        // <TestCaseDroid.test.Vulnerable: void main(java.lang.String[])>
        // <TestCaseDroid.test.CFG: void method1(int,int)>
        String pattern = "<(.*): (.*?) (.*?)\\((.*?)\\)>";
        // 特殊字符
        // <TestCaseDroid.test.ICFG: void <clinit>()>
        // <org.apache.commons.beanutils2.BeanUtilsBean:<init>(org.apache.commons.beanutils2.ConvertUtilsBean)>
        String initPattern = "<([^:]*):\\s*(.*?)\\s*(<\\w+>)\\((.*)\\)>";
        String clinitPattern = "<(.*): (.*?) (<clinit>)\\(\\)>";
        Pattern r;
        Matcher m;
        if (methodSignature.contains("<clinit>")) {
            r = Pattern.compile(clinitPattern);
            m = r.matcher(methodSignature);
            if (!m.matches()) {
                throw new IllegalArgumentException("Invalid method methodSignature: " + methodSignature);
            }
            this.className = m.group(1);
            this.returnType = m.group(2);
            this.methodName = m.group(3);
            this.paramTypes = new ArrayList<>();
        } else if (methodSignature.contains("<init>")) {
            r = Pattern.compile(initPattern);
            m = r.matcher(methodSignature);
            if (!m.matches()) {
                throw new IllegalArgumentException("Invalid method methodSignature: " + methodSignature);
            }
            this.className = m.group(1);
            this.methodName = m.group(3);
            this.paramTypes = new ArrayList<>();
            if (!m.group(4).isEmpty()) {
                String[] paramArray = m.group(4).split(",");
                this.paramTypes.addAll(Arrays.asList(paramArray));
            }
        } else {
            r = Pattern.compile(pattern);
            m = r.matcher(methodSignature);
            if (!m.matches()) {
                throw new IllegalArgumentException("Invalid method methodSignature: " + methodSignature);
            }
            this.className = m.group(1);
            this.returnType = m.group(2);
            this.methodName = m.group(3);
            this.paramTypes = new ArrayList<>();
            if (!m.group(4).isEmpty()) {
                String[] paramArray = m.group(4).split(",");
                this.paramTypes.addAll(Arrays.asList(paramArray));
            }
        }
    }

    public static void main(String[] args) {
        MethodContext methodContext = new MethodContext(
                "<TestCaseDroid.test.CallGraphs: void main(java.lang.String[],int)>");
        System.out.println(methodContext.className);
        System.out.println(methodContext.methodName);
        System.out.println(methodContext.returnType);
        System.out.println(methodContext.paramTypes);
    }
}
