package TestCaseDroid.script;

import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;


import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

/**
 * Utilize reflection mechanism and JUnit5 API to execute single test class.
 */
// Input parameters
// Dependency import issues

@Deprecated
@SuppressWarnings("deprecation")
public class TestRunner {
    public static void main(String[] args) throws Exception {
        TestExecutionSummary summary1= runSingleTest("E:\\Tutorial\\taitest\\","test.CFGTest");
        TestExecutionSummary summary2= runSingleTest("E:\\Tutorial\\taitest\\","test.CFGTest");
        CompareTestResult(summary1,summary2);
    }

    public  static void CompareTestResult(TestExecutionSummary summary1,TestExecutionSummary summary2){
        System.out.println("\n-----------------Compare Test Result-----------------");
        if(summary1.getTestsSucceededCount()==summary2.getTestsSucceededCount()){
            System.out.println("Test1 and Test2 are the same");
            System.out.println("Passed: "+summary1.getTestsSucceededCount()+" Failed: "+summary1.getTestsFailedCount());
        }else{
            System.out.println("Test1 and Test2 are different");
            System.out.println("Test1: "+"Passed: "+summary1.getTestsSucceededCount()+" Failed: "+summary1.getTestsFailedCount());
            System.out.println("Test2: "+"Passed: "+summary2.getTestsSucceededCount()+" Failed: "+summary2.getTestsFailedCount());
        }
        System.out.println("-----------------------------------------------------\n");
    }

    /**
     * Execute a single test.
     * @param ProjPath The project path
     * @param className The class name
     * @throws Exception Exceptions
     */
    private static TestExecutionSummary runSingleTest(String ProjPath,String className) throws Exception {
        Class<?> testClass = getTestClassFromPath(ProjPath, className);
        assert testClass != null;
        return runTest(testClass);
    }

    /**
     * Get the test class from the classpath and class name.
     * @param ProjPath The project path
     * @param className The class name eg: com.example.Test
     * @return The test class
     * @throws Exception Exceptions
     */
    private static Class<?> getTestClassFromPath(String ProjPath,String className) throws Exception {
        String testClassPath = ProjPath + File.separator + "target"+File.separator+"test-classes";
        String classPath = ProjPath + File.separator + "target"+File.separator+"classes";
        // Check if the path exists
        File testFile = new File(testClassPath);
        File classFile = new File(classPath);
        if (!testFile.exists() && !classFile.exists()) {
            System.out.println("Class file not found");
            return null;
        }
        //先引入项目的target/test-classes

        //再引入项目的target/classes

        // Load the class
        URL testUrl = testFile.toURI().toURL();
        URL classUrl = classFile.toURI().toURL();

        try (URLClassLoader classLoader = new URLClassLoader(new URL[]{testUrl,classUrl})) {
            return classLoader.loadClass(className+"Test");
        }catch (Exception e){
            System.out.println("Class not found");
            return null;
        }
    }

    /**
     * Run the test.
     * @param testClass The test class
     */
    private static TestExecutionSummary runTest(Class<?> testClass) {
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
        return listener.getSummary();
    }
}