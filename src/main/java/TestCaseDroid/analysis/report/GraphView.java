package TestCaseDroid.analysis.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A presentation-ready graph at package, class, method or call-site level.
 */
public final class GraphView {
    private final CallGraphGranularity granularity;
    private final List<Node> nodes;
    private final List<Edge> edges;

    public GraphView(CallGraphGranularity granularity, List<Node> nodes, List<Edge> edges) {
        this.granularity = granularity;
        this.nodes = Collections.unmodifiableList(new ArrayList<>(nodes));
        this.edges = Collections.unmodifiableList(new ArrayList<>(edges));
    }

    public CallGraphGranularity getGranularity() {
        return granularity;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public Node findNode(String id) {
        for (Node node : nodes) {
            if (node.getId().equals(id)) {
                return node;
            }
        }
        return null;
    }

    public static final class Node {
        private final String id;
        private final String label;
        private final String kind;
        private final String group;
        private final int depth;
        private final boolean application;
        private final boolean library;
        private final boolean constructor;
        private final boolean recursive;
        private final Map<String, Object> details;
        private int fanIn;
        private int fanOut;

        public Node(String id, String label, String kind, String group, int depth,
                    boolean application, boolean library, boolean constructor,
                    boolean recursive, Map<String, Object> details) {
            this.id = id;
            this.label = label;
            this.kind = kind;
            this.group = group;
            this.depth = depth;
            this.application = application;
            this.library = library;
            this.constructor = constructor;
            this.recursive = recursive;
            this.details = Collections.unmodifiableMap(new LinkedHashMap<>(details));
        }

        public String getId() {
            return id;
        }

        public String getLabel() {
            return label;
        }

        public String getKind() {
            return kind;
        }

        public String getGroup() {
            return group;
        }

        public int getDepth() {
            return depth;
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

        public boolean isRecursive() {
            return recursive;
        }

        public Map<String, Object> getDetails() {
            return details;
        }

        public int getFanIn() {
            return fanIn;
        }

        public int getFanOut() {
            return fanOut;
        }

        void setFanIn(int fanIn) {
            this.fanIn = fanIn;
        }

        void setFanOut(int fanOut) {
            this.fanOut = fanOut;
        }
    }

    public static final class Edge {
        private final String id;
        private final String source;
        private final String target;
        private final String kind;
        private final String label;
        private final int count;
        private final int sourceLine;
        private final String callSite;

        public Edge(String id, String source, String target, String kind,
                    String label, int count, int sourceLine, String callSite) {
            this.id = id;
            this.source = source;
            this.target = target;
            this.kind = kind;
            this.label = label;
            this.count = count;
            this.sourceLine = sourceLine;
            this.callSite = callSite;
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

        public String getLabel() {
            return label;
        }

        public int getCount() {
            return count;
        }

        public int getSourceLine() {
            return sourceLine;
        }

        public String getCallSite() {
            return callSite;
        }
    }
}
