package TestCaseDroid.analysis.reachability;

import org.junit.jupiter.api.Test;
import soot.Scene;
import soot.SootMethod;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
class BackwardReachabilityICFGTest {
    @Test
    void testBackwardReachabilityICFG() {
        BackwardReachabilityICFG reachability = new BackwardReachabilityICFG("TestCaseDroid.test.Vulnerable","E:\\Tutorial\\TestCaseDroid\\target\\classes");
        SootMethod source = Scene.v().getSootClass("TestCaseDroid.test.ICFG").getMethodByName("test1");
        SootMethod target = Scene.v().getSootClass("TestCaseDroid.test.ICFG").getMethodByName("test3");
        List<Context> reachedContext = reachability.inDynamicExtent(source, target);
        if (reachedContext != null && !reachedContext.isEmpty()) {
            System.out.println("The source method can be reached from the target method.");
            for (Context context : reachedContext) {
                System.out.println(context.getMethodCallStackString());
            }
        } else {
            System.out.println("The source method cannot be reached from the target method.");
        }
    }

}