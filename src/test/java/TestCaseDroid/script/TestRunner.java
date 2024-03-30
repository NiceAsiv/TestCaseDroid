package TestCaseDroid.script;

import TestCaseDroid.config.SootConfigTest;
import TestCaseDroid.graph.BuildCallGraphTest;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

public class TestRunner {
    public static void main(String[] args) {
        runTest(BuildCallGraphTest.class);
        runTest(SootConfigTest.class);
    }

    private static void runTest(Class<?> testClass) {
        System.out.println("Running tests for class " + testClass.getName());
        Launcher launcher = LauncherFactory.create(); // Create a Launcher
        // Create a SummaryGeneratingListener
        SummaryGeneratingListener listener = new SummaryGeneratingListener();

        // Create a LauncherDiscoveryRequest that includes the test class
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClass(testClass))
                .build();

        // Register the listener with the Launcher
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request); // Execute the tests

        // Get the execute summary
        TestExecutionSummary summary = listener.getSummary();
        System.out.println("运行的测试数量: " + summary.getTestsFoundCount());
        System.out.println("成功的测试数量: " + summary.getTestsSucceededCount());
        System.out.println("失败的测试数量: " + summary.getTestsFailedCount());
        System.out.println("忽略的测试数量: " + summary.getTestsSkippedCount());
        System.out.println("执行时间: " + summary.getTimeStarted());
        summary.getFailures().forEach(failure -> System.out.println(failure.getException()));
    }
}