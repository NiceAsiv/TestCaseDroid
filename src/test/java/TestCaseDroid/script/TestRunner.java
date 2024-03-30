package TestCaseDroid.script;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

public class TestRunner {
    public static void main(String[] args) throws Exception {
        List<Class<?>> testClasses = getTestClassesFromPath("/path/to/your/test/classes");
        for (Class<?> testClass : testClasses) {
            runTest(testClass);
        }
    }
    public static void runAllTests(String testClassOnePath ,String testClassTwoPath) throws Exception {
        List<Class<?>> testClassOne = getTestClassesFromPath(testClassOnePath);
        List<Class<?>> testClassTwo = getTestClassesFromPath(testClassTwoPath);
        for (Class<?> testClass : testClassOne) {
            runTest(testClass);
        }
        for (Class<?> testClass : testClassTwo) {
            runTest(testClass);
        }
    }

    private static List<Class<?>> getTestClassesFromPath(String path) throws Exception {
        List<Class<?>> classes = new ArrayList<>();
        URL url = TestRunner.class.getResource(path);
        assert url != null;
        File dir = new File(url.toURI());
        for (File file : Objects.requireNonNull(dir.listFiles())) {
            if (file.getName().endsWith(".class")) {
                String className = file.getName().replace(".class", "");
                classes.add(Class.forName(className));
            }
        }
        return classes;
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