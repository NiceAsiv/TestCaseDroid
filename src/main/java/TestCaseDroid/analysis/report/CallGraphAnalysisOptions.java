package TestCaseDroid.analysis.report;

/**
 * Bounded extraction options. Defaults favour an application-only graph that
 * remains useful on screen and in an AI context window.
 */
public final class CallGraphAnalysisOptions {
    private int maxDepth = 12;
    private int maxNodes = 2000;
    private boolean includeLibraries;
    private boolean includeConstructors;

    public int getMaxDepth() {
        return maxDepth;
    }

    public CallGraphAnalysisOptions setMaxDepth(int maxDepth) {
        if (maxDepth < 0) {
            throw new IllegalArgumentException("maxDepth must be >= 0");
        }
        this.maxDepth = maxDepth;
        return this;
    }

    public int getMaxNodes() {
        return maxNodes;
    }

    public CallGraphAnalysisOptions setMaxNodes(int maxNodes) {
        if (maxNodes < 1) {
            throw new IllegalArgumentException("maxNodes must be >= 1");
        }
        this.maxNodes = maxNodes;
        return this;
    }

    public boolean isIncludeLibraries() {
        return includeLibraries;
    }

    public CallGraphAnalysisOptions setIncludeLibraries(boolean includeLibraries) {
        this.includeLibraries = includeLibraries;
        return this;
    }

    public boolean isIncludeConstructors() {
        return includeConstructors;
    }

    public CallGraphAnalysisOptions setIncludeConstructors(boolean includeConstructors) {
        this.includeConstructors = includeConstructors;
        return this;
    }
}
