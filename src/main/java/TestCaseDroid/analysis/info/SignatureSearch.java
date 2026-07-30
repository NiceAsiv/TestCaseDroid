package TestCaseDroid.analysis.info;

import TestCaseDroid.config.SootConfig;
import soot.Scene;
import soot.SootClass;
import soot.SootMethod;
import soot.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves IntelliJ-style references such as
 * {@code com.example.Service#load(java.lang.String)} to Soot signatures.
 */
public class SignatureSearch {
    private static final Pattern WITH_PARAMS = Pattern.compile("^(.+)#([\\w$]+)\\((.*)\\)$");
    private static final Pattern WITHOUT_PARAMS = Pattern.compile("^(.+)#([\\w$]+)$");

    private final String className;
    private final String methodName;

    public SignatureSearch(String entryClassName, String methodName, String classPath) {
        if (isBlank(entryClassName) || isBlank(methodName)) {
            throw new IllegalArgumentException("Class name and method name must not be blank");
        }
        this.className = entryClassName.trim();
        this.methodName = methodName.trim();
        new SootConfig().setupSoot(this.className, false, classPath);
    }

    public static Boolean parseIDEARef(String ideaRef) {
        return parseReference(ideaRef) != null;
    }

    public static String getMethodSignatureByIDEARef(String ideaRef, String classPath) {
        ParsedReference reference = parseReference(ideaRef);
        if (reference == null) {
            return null;
        }

        new SootConfig().setupSoot(reference.className, false, classPath);
        SootClass sootClass = Scene.v().getSootClass(reference.className);
        String sootMethodName = reference.constructor ? "<init>" : reference.methodName;
        List<SootMethod> matches = new ArrayList<>();
        for (SootMethod method : sootClass.getMethods()) {
            if (!method.getName().equals(sootMethodName)) {
                continue;
            }
            if (reference.parameterTypes == null
                    || parameterTypesMatch(method.getParameterTypes(), reference.parameterTypes)) {
                matches.add(method);
            }
        }
        return matches.size() == 1 ? matches.get(0).getSignature() : null;
    }

    /** Returns all signatures matching the configured method name. */
    public List<String> findMethodSignatures() {
        SootClass sootClass = Scene.v().getSootClass(className);
        List<String> result = new ArrayList<>();
        for (SootMethod method : sootClass.getMethods()) {
            if (method.getName().equals(methodName)) {
                result.add(method.getSignature());
            }
        }
        Collections.sort(result);
        return result;
    }

    /** Legacy console-oriented API retained for CLI compatibility. */
    public void getMethodSignature() {
        List<String> result = findMethodSignatures();
        if (result.isEmpty()) {
            System.out.println("No method found, please check the class name and method name.");
            return;
        }
        System.out.println("Found " + result.size() + " methods:");
        for (String signature : result) {
            System.out.println(signature);
        }
    }

    private static boolean parameterTypesMatch(List<Type> actual, List<String> requested) {
        if (actual.size() != requested.size()) {
            return false;
        }
        for (int i = 0; i < actual.size(); i++) {
            if (!actual.get(i).toString().equals(requested.get(i))) {
                return false;
            }
        }
        return true;
    }

    private static ParsedReference parseReference(String ideaRef) {
        if (isBlank(ideaRef)) {
            return null;
        }
        String value = ideaRef.trim();
        Matcher withParams = WITH_PARAMS.matcher(value);
        if (withParams.matches()) {
            String parsedClass = withParams.group(1).trim();
            String parsedMethod = withParams.group(2).trim();
            List<String> parameters;
            try {
                parameters = parseParameters(withParams.group(3));
            } catch (IllegalArgumentException ignored) {
                return null;
            }
            return new ParsedReference(parsedClass, parsedMethod, parameters,
                    simpleName(parsedClass).equals(parsedMethod));
        }

        Matcher withoutParams = WITHOUT_PARAMS.matcher(value);
        if (!withoutParams.matches()) {
            return null;
        }
        String parsedClass = withoutParams.group(1).trim();
        String parsedMethod = withoutParams.group(2).trim();
        return new ParsedReference(parsedClass, parsedMethod, null,
                simpleName(parsedClass).equals(parsedMethod));
    }

    private static List<String> parseParameters(String raw) {
        if (raw.trim().isEmpty()) {
            return new ArrayList<>();
        }
        List<String> result = new ArrayList<>();
        int genericDepth = 0;
        int start = 0;
        for (int i = 0; i < raw.length(); i++) {
            char ch = raw.charAt(i);
            if (ch == '<') {
                genericDepth++;
            } else if (ch == '>') {
                if (--genericDepth < 0) {
                    throw new IllegalArgumentException("Unbalanced generic type");
                }
            } else if (ch == ',' && genericDepth == 0) {
                addParameter(result, raw.substring(start, i));
                start = i + 1;
            }
        }
        if (genericDepth != 0) {
            throw new IllegalArgumentException("Unbalanced generic type");
        }
        addParameter(result, raw.substring(start));
        return result;
    }

    private static void addParameter(List<String> target, String rawType) {
        String type = eraseGenerics(rawType.trim()).replace("...", "[]");
        if (type.isEmpty()) {
            throw new IllegalArgumentException("Empty parameter type");
        }
        target.add(type);
    }

    private static String eraseGenerics(String type) {
        StringBuilder result = new StringBuilder();
        int depth = 0;
        for (int i = 0; i < type.length(); i++) {
            char ch = type.charAt(i);
            if (ch == '<') {
                depth++;
            } else if (ch == '>') {
                depth--;
            } else if (depth == 0) {
                result.append(ch);
            }
        }
        if (depth != 0) {
            throw new IllegalArgumentException("Unbalanced generic type");
        }
        return result.toString().trim();
    }

    private static String simpleName(String className) {
        int dot = Math.max(className.lastIndexOf('.'), className.lastIndexOf('$'));
        return className.substring(dot + 1);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static final class ParsedReference {
        private final String className;
        private final String methodName;
        private final List<String> parameterTypes;
        private final boolean constructor;

        private ParsedReference(String className, String methodName,
                                List<String> parameterTypes, boolean constructor) {
            this.className = className;
            this.methodName = methodName;
            this.parameterTypes = parameterTypes;
            this.constructor = constructor;
        }
    }
}
