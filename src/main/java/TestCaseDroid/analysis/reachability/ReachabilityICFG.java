package TestCaseDroid.analysis.reachability;

import TestCaseDroid.config.SootConfig;
import lombok.Setter;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Context-sensitive reachability over Soot's interprocedural control-flow graph.
 *
 * <p>Call sites enter callee start nodes and remember the matching return site.
 * Exit nodes return only to that remembered site. This avoids the false paths
 * produced by flattening complete method bodies into one worklist.</p>
 */
public class ReachabilityICFG {
    private final JimpleBasedInterproceduralCFG icfg;
    @Setter
    private int maxDepth = 10_000;
    @Setter
    private int maxPaths = 100;

    public ReachabilityICFG(String targetClass) {
        new SootConfig().setupSoot(targetClass, true);
        this.icfg = new JimpleBasedInterproceduralCFG();
    }

    public ReachabilityICFG(String targetClass, String classPath) {
        new SootConfig().setupSoot(targetClass, true, classPath);
        this.icfg = new JimpleBasedInterproceduralCFG();
    }

    /**
     * Returns witness paths from any start node of {@code source} to
     * {@code target}. An empty list means unreachable.
     */
    public List<Context> inDynamicExtent(SootMethod source, SootMethod target) {
        if (source == null || target == null) {
            throw new IllegalArgumentException("Source and target methods must not be null");
        }
        validateBounds();
        List<Context> result = new ArrayList<>();
        for (Unit start : icfg.getStartPointsOf(source)) {
            result.addAll(reachable(new Context(start), target));
            if (result.size() >= maxPaths) {
                break;
            }
        }
        return result;
    }

    public List<Context> reachable(Context source, SootMethod target) {
        if (source == null || source.getReachedNode() == null || target == null) {
            throw new IllegalArgumentException("Source context and target method must not be null");
        }
        validateBounds();

        SootMethod sourceMethod = icfg.getMethodOf(source.getReachedNode());
        Deque<State> worklist = new ArrayDeque<>();
        worklist.add(State.start(source.getReachedNode(), sourceMethod));
        Set<StateKey> visited = new HashSet<>();
        List<Context> paths = new ArrayList<>();

        while (!worklist.isEmpty() && paths.size() < maxPaths) {
            State current = worklist.removeFirst();
            if (!visited.add(current.key())) {
                continue;
            }

            SootMethod reachedMethod = icfg.getMethodOf(current.node);
            if (reachedMethod.equals(target)) {
                paths.add(current.toContext(reachedMethod));
                continue;
            }
            if (current.depth >= maxDepth) {
                continue;
            }

            if (icfg.isCallStmt(current.node)) {
                enqueueCallTransitions(current, worklist);
            } else if (icfg.isExitStmt(current.node) && !current.returnSites.isEmpty()) {
                Unit returnSite = current.returnSites.get(current.returnSites.size() - 1);
                worklist.addLast(current.returnTo(returnSite));
            } else {
                for (Unit successor : icfg.getSuccsOf(current.node)) {
                    worklist.addLast(current.moveTo(successor));
                }
            }
        }
        return paths;
    }

    private void enqueueCallTransitions(State current, Deque<State> worklist) {
        Collection<SootMethod> callees = icfg.getCalleesOfCallAt(current.node);
        Collection<Unit> returnSites = icfg.getReturnSitesOfCallAt(current.node);
        boolean enteredCallee = false;

        for (SootMethod callee : callees) {
            if (!callee.isConcrete() || callee.isJavaLibraryMethod()) {
                continue;
            }
            for (Unit startPoint : icfg.getStartPointsOf(callee)) {
                if (returnSites.isEmpty()) {
                    worklist.addLast(current.call(startPoint, callee, null));
                } else {
                    for (Unit returnSite : returnSites) {
                        worklist.addLast(current.call(startPoint, callee, returnSite));
                    }
                }
                enteredCallee = true;
            }
        }

        // External/phantom calls have no body. Continue at their return sites.
        if (!enteredCallee) {
            for (Unit returnSite : returnSites) {
                worklist.addLast(current.moveTo(returnSite));
            }
        }
    }

    public void runAnalysis(MethodContext entryMethod, MethodContext targetMethod) {
        SootMethod source = Scene.v().getMethod(entryMethod.getMethodSignature());
        SootMethod target = Scene.v().getMethod(targetMethod.getMethodSignature());
        List<Context> reachedContexts = inDynamicExtent(source, target);
        if (reachedContexts.isEmpty()) {
            System.out.println("The target method cannot be reached from the source method.");
            return;
        }
        System.out.println("The target method can be reached from the source method.");
        for (Context context : reachedContexts) {
            System.out.println(context.getMethodCallStackString());
        }
    }

    private void validateBounds() {
        if (maxDepth < 0) {
            throw new IllegalStateException("maxDepth must be at least 0");
        }
        if (maxPaths < 1) {
            throw new IllegalStateException("maxPaths must be at least 1");
        }
    }

    private static final class State {
        private final Unit node;
        private final List<Unit> path;
        private final List<SootMethod> methodPath;
        private final List<Unit> returnSites;
        private final int depth;

        private State(Unit node, List<Unit> path, List<SootMethod> methodPath,
                      List<Unit> returnSites, int depth) {
            this.node = node;
            this.path = path;
            this.methodPath = methodPath;
            this.returnSites = returnSites;
            this.depth = depth;
        }

        private static State start(Unit node, SootMethod method) {
            return new State(node, Collections.singletonList(node),
                    Collections.singletonList(method), Collections.<Unit>emptyList(), 0);
        }

        private State moveTo(Unit next) {
            return new State(next, appended(path, next), methodPath, returnSites, depth + 1);
        }

        private State call(Unit calleeStart, SootMethod callee, Unit returnSite) {
            List<Unit> newReturnSites = returnSites;
            if (returnSite != null) {
                newReturnSites = appended(returnSites, returnSite);
            }
            return new State(calleeStart, appended(path, calleeStart),
                    appended(methodPath, callee), newReturnSites, depth + 1);
        }

        private State returnTo(Unit returnSite) {
            List<Unit> newReturnSites = new ArrayList<>(returnSites);
            newReturnSites.remove(newReturnSites.size() - 1);
            return new State(returnSite, appended(path, returnSite),
                    methodPath, newReturnSites, depth + 1);
        }

        private StateKey key() {
            return new StateKey(node, returnSites);
        }

        private Context toContext(SootMethod reachedMethod) {
            Context context = new Context(node, new LinkedList<>(path),
                    new LinkedList<>(methodPath));
            context.setReachedMethod(reachedMethod);
            return context;
        }

        private static <T> List<T> appended(List<T> source, T value) {
            List<T> result = new ArrayList<>(source);
            result.add(value);
            return result;
        }
    }

    private static final class StateKey {
        private final Unit node;
        private final List<Unit> returnSites;

        private StateKey(Unit node, List<Unit> returnSites) {
            this.node = node;
            this.returnSites = new ArrayList<>(returnSites);
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof StateKey)) {
                return false;
            }
            StateKey that = (StateKey) other;
            return Objects.equals(node, that.node)
                    && Objects.equals(returnSites, that.returnSites);
        }

        @Override
        public int hashCode() {
            return Objects.hash(node, returnSites);
        }
    }
}
