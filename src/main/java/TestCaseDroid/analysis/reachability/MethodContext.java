package TestCaseDroid.analysis.reachability;

import lombok.Getter;
import lombok.Setter;
import soot.SootMethod;
import soot.Unit;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Setter
@Getter
public class MethodContext {
    private String className;
    private String methodName;
    private String returnType;
    private String methodSignature; //current method signature
    private List<String> paramTypes;
    Boolean isBackwardReachability = true;
    private Deque<SootMethod> methodCallStack;

    /**
     * Constructor of MethodContext
     * @param methodSignature The method signature like "<TestCaseDroid.test.CallGraphs: void main(java.lang.String[])>"
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

    //copy
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
        }else {
            for (SootMethod method : methodCallStack) {
                sb.append(method.getSignature()).append(" -> ");
            }
        }
        sb.delete(sb.length() - 4, sb.length());
        return sb.toString();
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
        String pattern = "<(.*): (.*?) (.*?)\\((.*?)\\)>"; //正则表达式
        if (!methodSignature.matches(pattern)) {
            throw new IllegalArgumentException("Invalid method methodSignature: " + methodSignature);
        }
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(methodSignature);
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
            throw new IllegalArgumentException("Invalid method methodSignature: " + methodSignature);
        }
    }

    public static void main(String[] args) {
        MethodContext methodContext = new MethodContext("<TestCaseDroid.test.CallGraphs: void main(java.lang.String[],int)>");
        System.out.println(methodContext.className);
        System.out.println(methodContext.methodName);
        System.out.println(methodContext.returnType);
        System.out.println(methodContext.paramTypes);
    }
}
