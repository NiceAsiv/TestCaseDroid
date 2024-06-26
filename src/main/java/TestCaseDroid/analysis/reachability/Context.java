package TestCaseDroid.analysis.reachability;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;

import TestCaseDroid.utils.DotGraphWrapper;
import lombok.Getter;
import lombok.Setter;
import soot.SootMethod;
import soot.Unit;

/**
 * Context class represents the context of ICFG or BackWardICFG, including the reached node and the call stack.
 */
@Getter
@Setter
public class Context {
    private Deque<Unit> callStack;
    private Deque<SootMethod> methodCallStack;
    private Unit reachedNode;
    private SootMethod reachedMethod;
    private boolean isBackward = false;

    /**
     * Default constructor, initializes an empty call stack.
     */
    public Context() {
        callStack = new LinkedList<>();
    }

    /**
     * Constructor, initializes a reached node and an empty call stack.
     * @param reachedNode The reached node
     */
    public Context(Unit reachedNode) {
        this.reachedNode = reachedNode;
        callStack = new LinkedList<>();
        methodCallStack = new LinkedList<>();
    }

    /**
     * Constructor, initializes a reached node and a call stack.
     * @param reachedNode The reached node
     * @param callStack The call stack
     */
    public Context(Unit reachedNode, Deque<Unit> callStack) {
        this.reachedNode = reachedNode;
        this.callStack = callStack;
    }

    /**
     * Constructor, initializes a reached node, a call stack and a method call stack.
     * @param reachedNode The reached node
     * @param callStack The call stack
     * @param methodCallStack The method call stack
     */
    public Context(Unit reachedNode, Deque<Unit> callStack, Deque<SootMethod> methodCallStack) {
        this.reachedNode = reachedNode;
        this.callStack = callStack;
        this.methodCallStack = methodCallStack;
    }

    /**
     * Copies the current context.
     * @return A new context that is a copy of the current context.
     */
    public Context copy() {
        return new Context(this.reachedNode, new LinkedList<>(this.callStack), new LinkedList<>(this.methodCallStack));
    }

    /**
     * Gets the method call stack as a string.
     * @return The method call stack as a string.
     */
    public String getMethodCallStackString() {
        StringBuilder sb = new StringBuilder();
        SootMethod currentMethod = null;
        for (SootMethod method : getMethodCallStack()) {
            if (currentMethod != null && !currentMethod.equals(method)) {
                sb.append(currentMethod.getSignature()).append("\n -> ");
            }
            currentMethod = method;
        }
        if (sb.length() > 4) {
            //remove the last " -> "
            sb.delete(sb.length() - 4, sb.length());
        }
        return sb.toString();
    }

    public Deque<Unit> getReversedCallStack() {
        Deque<Unit> reversedCallStack = new LinkedList<>();
        for (Unit unit : callStack) {
            reversedCallStack.addFirst(unit);
        }
        return reversedCallStack;
    }

    /**
     * Checks if the current context is equal to the specified object.
     * @param obj The object to check
     * @return True if the current context is equal to the specified object, false otherwise.
     */
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Context) {
            Context other = (Context) obj;
            return Objects.equals(reachedNode, other.reachedNode) && callStack.equals(other.callStack);
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Reached node: ").append(reachedNode).append(" in method: ").append(reachedMethod).append("\n");
        if (!callStack.isEmpty()) {
            sb.append("Call stack: ");
            if (isBackward) {
                for (Unit unit : getReversedCallStack()) {
                    sb.append(unit).append("\n -> ");
                }
            } else {
                for (Unit unit : callStack) {
                    sb.append(unit).append("\n -> ");
                }
            }
            sb.delete(sb.length() - 4, sb.length());
        }
        return sb.toString();
    }

}