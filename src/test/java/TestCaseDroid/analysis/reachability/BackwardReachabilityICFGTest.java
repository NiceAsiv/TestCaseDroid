package TestCaseDroid.analysis.reachability;

import org.junit.jupiter.api.Test;
import soot.Scene;
import soot.SootMethod;

import static org.junit.jupiter.api.Assertions.*;
class BackwardReachabilityICFGTest {
    @Test
    void testBackwardReachabilityICFG() {
        BackwardReachabilityICFG reachability = new BackwardReachabilityICFG("TestCaseDroid.test.Vulnerable","E:\\Tutorial\\TestCaseDroid\\target\\classes");
        SootMethod source = Scene.v().getSootClass("TestCaseDroid.test.ICFG").getMethodByName("test1");
        SootMethod target = Scene.v().getSootClass("TestCaseDroid.test.ICFG").getMethodByName("test3");
        Context reachedContext = reachability.inDynamicExtent(source, target);
        if (reachedContext != null) {
            System.out.println("The source method can be reached from the target method.");
            System.out.println("The Call Stack is:\n " + reachedContext);
            System.out.println("The Method Call Stack is:\n " + reachedContext.getMethodCallStackString());
        } else {
            System.out.println("The source method cannot be reached from the target method.");
        }
    }

}