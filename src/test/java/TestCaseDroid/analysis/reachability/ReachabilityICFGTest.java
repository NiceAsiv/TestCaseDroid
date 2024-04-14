package TestCaseDroid.analysis.reachability;

import org.junit.jupiter.api.Test;
import soot.Scene;
import soot.SootMethod;

import java.util.List;


class ReachabilityICFGTest {

        @Test
        void getExecutionPathFromEntryPoint() {
        }

        @Test
        void inDynamicExtent() {
        }

        @Test
        void reachable() {

        }
        @Test
        void execute() {
            ReachabilityICFG ReachabilityICFG = new ReachabilityICFG("TestCaseDroid.test.Vulnerable");
            SootMethod source = Scene.v().getSootClass("TestCaseDroid.test.Vulnerable").getMethod("void main(java.lang.String[])");
            SootMethod target = Scene.v().getSootClass("TestCaseDroid.test.ICFG").getMethod("void test1()");
            List<Context> reachedContext = ReachabilityICFG.inDynamicExtent(source, target);
            if (reachedContext != null && !reachedContext.isEmpty()) {
                System.out.println("The target method can be reached from the source method.");
            } else {
                System.out.println("The target method cannot be reached from the source method.");
            }
        }

}