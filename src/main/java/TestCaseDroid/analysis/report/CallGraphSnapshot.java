package TestCaseDroid.analysis.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A tool-independent snapshot of a Soot call graph.
 *
 * <p>The snapshot is the common source for the Swing viewer, JSON reports and
 * Markdown context written for AI systems. Keeping this model independent from
 * Soot also makes presentation and export code deterministic and testable.</p>
 */
public final class CallGraphSnapshot {
    private final Metadata metadata;
    private final List<MethodNode> nodes;
    private final List<CallEdge> edges;
    private final List<List<String>> recursiveComponents;
    private final List<String> unreachableApplicationMethods;
    private final List<String> warnings;
    private final boolean truncated;

    public CallGraphSnapshot(Metadata metadata,
                             List<MethodNode> nodes,
                             List<CallEdge> edges,
                             List<List<String>> recursiveComponents,
                             List<String> unreachableApplicationMethods,
                             List<String> warnings,
                             boolean truncated) {
        this.metadata = metadata;
        this.nodes = Collections.unmodifiableList(new ArrayList<>(nodes));
        this.edges = Collections.unmodifiableList(new ArrayList<>(edges));
        this.recursiveComponents = immutableNestedList(recursiveComponents);
        this.unreachableApplicationMethods = Collections.unmodifiableList(
                new ArrayList<>(unreachableApplicationMethods));
        this.warnings = Collections.unmodifiableList(new ArrayList<>(warnings));
        this.truncated = truncated;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public List<MethodNode> getNodes() {
        return nodes;
    }

    public List<CallEdge> getEdges() {
        return edges;
    }

    public List<List<String>> getRecursiveComponents() {
        return recursiveComponents;
    }

    public List<String> getUnreachableApplicationMethods() {
        return unreachableApplicationMethods;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public boolean isTruncated() {
        return truncated;
    }

    public MethodNode findNode(String id) {
        for (MethodNode node : nodes) {
            if (node.getId().equals(id)) {
                return node;
            }
        }
        return null;
    }

    public Map<String, MethodNode> nodeIndex() {
        Map<String, MethodNode> result = new LinkedHashMap<>();
        for (MethodNode node : nodes) {
            result.put(node.getId(), node);
        }
        return result;
    }

    private static List<List<String>> immutableNestedList(List<List<String>> values) {
        List<List<String>> result = new ArrayList<>();
        for (List<String> value : values) {
            result.add(Collections.unmodifiableList(new ArrayList<>(value)));
        }
        return Collections.unmodifiableList(result);
    }

    public static final class Metadata {
        private final String entryClass;
        private final String entryMethod;
        private final String algorithm;
        private final String classPath;
        private final int maxDepth;
        private final int maxNodes;
        private final boolean includesLibraries;
        private final boolean includesConstructors;

        public Metadata(String entryClass, String entryMethod, String algorithm,
                        String classPath, int maxDepth, int maxNodes,
                        boolean includesLibraries, boolean includesConstructors) {
            this.entryClass = entryClass;
            this.entryMethod = entryMethod;
            this.algorithm = algorithm;
            this.classPath = classPath;
            this.maxDepth = maxDepth;
            this.maxNodes = maxNodes;
            this.includesLibraries = includesLibraries;
            this.includesConstructors = includesConstructors;
        }

        public String getEntryClass() {
            return entryClass;
        }

        public String getEntryMethod() {
            return entryMethod;
        }

        public String getAlgorithm() {
            return algorithm;
        }

        public String getClassPath() {
            return classPath;
        }

        public int getMaxDepth() {
            return maxDepth;
        }

        public int getMaxNodes() {
            return maxNodes;
        }

        public boolean isIncludesLibraries() {
            return includesLibraries;
        }

        public boolean isIncludesConstructors() {
            return includesConstructors;
        }
    }

    public static final class MethodNode {
        private final String id;
        private final String packageName;
        private final String className;
        private final String methodName;
        private final String subSignature;
        private final String returnType;
        private final List<String> parameterTypes;
        private final String modifiers;
        private final boolean application;
        private final boolean library;
        private final boolean constructor;
        private final boolean concrete;
        private final int depth;
        private final int sourceLine;
        private int fanIn;
        private int fanOut;
        private boolean recursive;

        public MethodNode(String id, String packageName, String className,
                          String methodName, String subSignature, String returnType,
                          List<String> parameterTypes, String modifiers,
                          boolean application, boolean library, boolean constructor,
                          boolean concrete, int depth, int sourceLine) {
            this.id = id;
            this.packageName = packageName;
            this.className = className;
            this.methodName = methodName;
            this.subSignature = subSignature;
            this.returnType = returnType;
            this.parameterTypes = Collections.unmodifiableList(new ArrayList<>(parameterTypes));
            this.modifiers = modifiers;
            this.application = application;
            this.library = library;
            this.constructor = constructor;
            this.concrete = concrete;
            this.depth = depth;
            this.sourceLine = sourceLine;
        }

        public String getId() {
            return id;
        }

        public String getLabel() {
            return className.substring(className.lastIndexOf('.') + 1)
                    + "." + methodName + "(" + String.join(",", parameterTypes) + ")";
        }

        public String getPackageName() {
            return packageName;
        }

        public String getClassName() {
            return className;
        }

        public String getMethodName() {
            return methodName;
        }

        public String getSubSignature() {
            return subSignature;
        }

        public String getReturnType() {
            return returnType;
        }

        public List<String> getParameterTypes() {
            return parameterTypes;
        }

        public String getModifiers() {
            return modifiers;
        }

        public boolean isApplication() {
            return application;
        }

        public boolean isLibrary() {
            return library;
        }

        public boolean isConstructor() {
            return constructor;
        }

        public boolean isConcrete() {
            return concrete;
        }

        public int getDepth() {
            return depth;
        }

        public int getSourceLine() {
            return sourceLine;
        }

        public int getFanIn() {
            return fanIn;
        }

        public int getFanOut() {
            return fanOut;
        }

        public boolean isRecursive() {
            return recursive;
        }

        void setFanIn(int fanIn) {
            this.fanIn = fanIn;
        }

        void setFanOut(int fanOut) {
            this.fanOut = fanOut;
        }

        void setRecursive(boolean recursive) {
            this.recursive = recursive;
        }
    }

    public static final class CallEdge {
        private final String id;
        private final String source;
        private final String target;
        private final String kind;
        private final String callSite;
        private final int sourceLine;
        private final boolean sourceApplication;
        private final boolean targetApplication;

        public CallEdge(String id, String source, String target, String kind,
                        String callSite, int sourceLine,
                        boolean sourceApplication, boolean targetApplication) {
            this.id = id;
            this.source = source;
            this.target = target;
            this.kind = kind;
            this.callSite = callSite;
            this.sourceLine = sourceLine;
            this.sourceApplication = sourceApplication;
            this.targetApplication = targetApplication;
        }

        public String getId() {
            return id;
        }

        public String getSource() {
            return source;
        }

        public String getTarget() {
            return target;
        }

        public String getKind() {
            return kind;
        }

        public String getCallSite() {
            return callSite;
        }

        public int getSourceLine() {
            return sourceLine;
        }

        public boolean isSourceApplication() {
            return sourceApplication;
        }

        public boolean isTargetApplication() {
            return targetApplication;
        }
    }
}
