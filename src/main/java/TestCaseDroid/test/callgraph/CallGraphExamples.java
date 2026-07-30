package TestCaseDroid.test.callgraph;

/**
 * Small, dependency-free programs used by the README and integration tests.
 * Each entry method demonstrates a different call-graph shape.
 */
public final class CallGraphExamples {
    private CallGraphExamples() {
    }

    /** Direct edge plus two branches that merge before reaching the same sink. */
    public static void diamondEntry() {
        direct();
        left();
        right();
    }

    private static void direct() {
        diamondSink();
    }

    private static void left() {
        shared();
    }

    private static void right() {
        shared();
    }

    private static void shared() {
        diamondSink();
    }

    private static void diamondSink() {
        // analysis target
    }

    /** A recursive cycle with an exit to a reachable sink. */
    public static void recursiveEntry() {
        recursiveA(1);
    }

    private static void recursiveA(int depth) {
        recursiveB(depth);
    }

    private static void recursiveB(int depth) {
        if (depth > 0) {
            recursiveA(depth - 1);
        }
        recursiveSink();
    }

    private static void recursiveSink() {
        // analysis target
    }

    /** Only the int overload is called. */
    public static void overloadEntry() {
        overloaded(1);
    }

    private static void overloaded(int value) {
        intOverloadSink();
    }

    private static void overloaded(String value) {
        stringOverloadSink();
    }

    private static void intOverloadSink() {
        // analysis target
    }

    private static void stringOverloadSink() {
        // intentionally unreachable from overloadEntry
    }

    /** Interface dispatch; CHA conservatively includes compatible implementations. */
    public static void interfaceEntry() {
        Service service = new PrimaryService();
        service.execute();
    }

    interface Service {
        void execute();
    }

    static final class PrimaryService implements Service {
        @Override
        public void execute() {
            interfaceSink();
        }
    }

    static final class SecondaryService implements Service {
        @Override
        public void execute() {
            secondarySink();
        }
    }

    private static void interfaceSink() {
        // analysis target
    }

    private static void secondarySink() {
        // another possible CHA target
    }

    /** Branch and loop examples used by CFG tests. */
    public static void cfgEntry(int count) {
        if (count > 0) {
            while (count-- > 0) {
                cfgWork();
            }
            cfgSink();
        } else {
            cfgOther();
        }
    }

    private static void cfgWork() {
        // loop body
    }

    private static void cfgSink() {
        // analysis target
    }

    private static void cfgOther() {
        // non-target branch
    }

    public static void unreachableSink() {
        // no entry method calls this method
    }
}
