package TestCaseDroid.analysis.reachability;

import lombok.Getter;
import lombok.Setter;
import soot.SootMethod;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parsed representation of a Soot method signature plus an optional call path.
 */
@Setter
@Getter
public class MethodContext {
    private static final Pattern SIGNATURE = Pattern.compile(
            "^<([^:]+):\\s*(?:(\\S+)\\s+)?([^\\s(]+)\\((.*)\\)>$");

    private String className;
    private String methodName;
    private String returnType;
    private String methodSignature;
    private List<String> paramTypes;
    private Boolean isBackwardReachability = true;
    private Deque<SootMethod> methodCallStack;

    public MethodContext(String methodSignature) {
        setMethodSignature(methodSignature);
        this.methodCallStack = new ArrayDeque<>();
    }

    public MethodContext(String methodSignature, Deque<SootMethod> methodCallStack) {
        setMethodSignature(methodSignature);
        this.methodCallStack = methodCallStack == null
                ? new ArrayDeque<SootMethod>()
                : new ArrayDeque<>(methodCallStack);
    }

    public MethodContext(String className, String methodName, String returnType, List<String> paramTypes) {
        if (isBlank(className) || isBlank(methodName)) {
            throw new IllegalArgumentException("Class name and method name must not be blank");
        }
        this.className = className.trim();
        this.methodName = methodName.trim();
        this.returnType = returnType == null ? null : returnType.trim();
        this.paramTypes = paramTypes == null
                ? new ArrayList<String>()
                : new ArrayList<>(paramTypes);
        this.methodSignature = buildSignature();
        this.methodCallStack = new ArrayDeque<>();
    }

    public final void setMethodSignature(String methodSignature) {
        if (isBlank(methodSignature)) {
            throw new IllegalArgumentException("Method signature must not be blank");
        }
        String normalized = methodSignature.trim();
        Matcher matcher = SIGNATURE.matcher(normalized);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid Soot method signature: " + methodSignature);
        }

        String parsedMethodName = matcher.group(3);
        String parsedReturnType = matcher.group(2);
        boolean constructor = "<init>".equals(parsedMethodName);
        if (!constructor && parsedReturnType == null) {
            throw new IllegalArgumentException("Missing return type in method signature: " + methodSignature);
        }

        this.methodSignature = normalized;
        this.className = matcher.group(1).trim();
        this.returnType = parsedReturnType;
        this.methodName = parsedMethodName;
        this.paramTypes = splitParameters(matcher.group(4));
    }

    public MethodContext copy() {
        MethodContext copy = new MethodContext(methodSignature, methodCallStack);
        copy.isBackwardReachability = isBackwardReachability;
        return copy;
    }

    public Deque<SootMethod> getReverseMethodCallStack() {
        Deque<SootMethod> reverse = new ArrayDeque<>();
        for (SootMethod method : methodCallStack) {
            reverse.addFirst(method);
        }
        return reverse;
    }

    public String getMethodCallStackString() {
        Iterable<SootMethod> methods = Boolean.TRUE.equals(isBackwardReachability)
                ? getReverseMethodCallStack()
                : methodCallStack;
        StringBuilder result = new StringBuilder();
        for (SootMethod method : methods) {
            if (result.length() > 0) {
                result.append(" -> ");
            }
            result.append(method.getSignature());
        }
        return result.toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(methodSignature, methodCallStack);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MethodContext)) {
            return false;
        }
        MethodContext that = (MethodContext) obj;
        return Objects.equals(methodSignature, that.methodSignature)
                && Objects.equals(methodCallStack, that.methodCallStack);
    }

    private String buildSignature() {
        StringBuilder value = new StringBuilder("<").append(className).append(": ");
        if (!isBlank(returnType)) {
            value.append(returnType).append(' ');
        }
        value.append(methodName).append('(');
        for (int i = 0; i < paramTypes.size(); i++) {
            if (i > 0) {
                value.append(',');
            }
            value.append(paramTypes.get(i).trim());
        }
        return value.append(")>").toString();
    }

    private static List<String> splitParameters(String parameters) {
        if (parameters == null || parameters.trim().isEmpty()) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        int genericDepth = 0;
        int start = 0;
        for (int i = 0; i < parameters.length(); i++) {
            char ch = parameters.charAt(i);
            if (ch == '<') {
                genericDepth++;
            } else if (ch == '>') {
                genericDepth--;
            } else if (ch == ',' && genericDepth == 0) {
                addParameter(result, parameters.substring(start, i));
                start = i + 1;
            }
        }
        if (genericDepth != 0) {
            throw new IllegalArgumentException("Unbalanced generic parameter list: " + parameters);
        }
        addParameter(result, parameters.substring(start));
        return result;
    }

    private static void addParameter(List<String> result, String value) {
        String parameter = value.trim();
        if (parameter.isEmpty()) {
            throw new IllegalArgumentException("Empty parameter type");
        }
        result.add(parameter);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
