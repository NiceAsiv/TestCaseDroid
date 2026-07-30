package TestCaseDroid.graph;

import TestCaseDroid.analysis.reachability.MethodContext;
import TestCaseDroid.config.SootConfig;
import TestCaseDroid.utils.DotGraphWrapper;
import soot.Scene;
import soot.SootMethod;
import soot.Unit;
import soot.jimple.toolkits.ide.icfg.JimpleBasedInterproceduralCFG;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/** Exports a context-sensitive ICFG rooted at one method. */
public final class BuildICFG {
    private static final int MAX_CONTEXT_DEPTH = 100;

    private BuildICFG() {
    }

    public static void buildICFGForClass(String inputPath, String classNameForAnalysis,
                                         MethodContext entryMethod) {
        if (entryMethod == null) {
            throw new IllegalArgumentException("Entry method must not be null");
        }
        SootConfig config = new SootConfig();
        config.setupSoot(classNameForAnalysis, true, inputPath,
                Collections.singletonList(entryMethod.getMethodSignature()));

        SootMethod root = Scene.v().getMethod(entryMethod.getMethodSignature());
        if (!root.isConcrete()) {
            throw new IllegalArgumentException("Entry method has no body: " + root.getSignature());
        }

        JimpleBasedInterproceduralCFG icfg = new JimpleBasedInterproceduralCFG();
        DotGraphWrapper graph = new DotGraphWrapper(
                "ICFG of " + entryMethod.getMethodSignature());
        Deque<State> worklist = new ArrayDeque<>();
        for (Unit start : icfg.getStartPointsOf(root)) {
            worklist.add(new State(start, Collections.<Unit>emptyList(), 0));
            graph.drawNode(start.toString());
        }
        Set<State> visited = new HashSet<>();

        while (!worklist.isEmpty()) {
            State state = worklist.removeFirst();
            if (!visited.add(state) || state.depth > MAX_CONTEXT_DEPTH) {
                continue;
            }
            Unit node = state.node;
            if (icfg.isCallStmt(node)) {
                enqueueCallEdges(icfg, graph, state, worklist);
            } else if (icfg.isExitStmt(node) && !state.returnSites.isEmpty()) {
                Unit returnSite = state.returnSites.get(state.returnSites.size() - 1);
                drawAndEnqueue(graph, worklist, node, returnSite,
                        new State(returnSite, withoutLast(state.returnSites), state.depth + 1));
            } else {
                for (Unit successor : icfg.getSuccsOf(node)) {
                    drawAndEnqueue(graph, worklist, node, successor,
                            new State(successor, state.returnSites, state.depth + 1));
                }
            }
        }
        graph.plot("icfg", classNameForAnalysis + "." + entryMethod.getMethodName());
    }

    private static void enqueueCallEdges(JimpleBasedInterproceduralCFG icfg,
                                         DotGraphWrapper graph, State state,
                                         Deque<State> worklist) {
        Collection<Unit> returnSites = icfg.getReturnSitesOfCallAt(state.node);
        boolean entered = false;
        for (SootMethod callee : icfg.getCalleesOfCallAt(state.node)) {
            if (!callee.isConcrete() || callee.isJavaLibraryMethod()) {
                continue;
            }
            for (Unit start : icfg.getStartPointsOf(callee)) {
                if (returnSites.isEmpty()) {
                    drawAndEnqueue(graph, worklist, state.node, start,
                            new State(start, state.returnSites, state.depth + 1));
                } else {
                    for (Unit returnSite : returnSites) {
                        List<Unit> stack = new ArrayList<>(state.returnSites);
                        stack.add(returnSite);
                        drawAndEnqueue(graph, worklist, state.node, start,
                                new State(start, stack, state.depth + 1));
                    }
                }
                entered = true;
            }
        }
        if (!entered) {
            for (Unit returnSite : returnSites) {
                drawAndEnqueue(graph, worklist, state.node, returnSite,
                        new State(returnSite, state.returnSites, state.depth + 1));
            }
        }
    }

    private static void drawAndEnqueue(DotGraphWrapper graph, Deque<State> worklist,
                                       Unit source, Unit target, State next) {
        graph.drawNode(source.toString());
        graph.drawNode(target.toString());
        graph.drawEdge(source.toString(), target.toString());
        worklist.addLast(next);
    }

    private static List<Unit> withoutLast(List<Unit> source) {
        List<Unit> result = new ArrayList<>(source);
        result.remove(result.size() - 1);
        return result;
    }

    private static final class State {
        private final Unit node;
        private final List<Unit> returnSites;
        private final int depth;

        private State(Unit node, List<Unit> returnSites, int depth) {
            this.node = node;
            this.returnSites = new ArrayList<>(returnSites);
            this.depth = depth;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof State)) {
                return false;
            }
            State that = (State) other;
            return Objects.equals(node, that.node)
                    && Objects.equals(returnSites, that.returnSites);
        }

        @Override
        public int hashCode() {
            return Objects.hash(node, returnSites);
        }
    }
}
