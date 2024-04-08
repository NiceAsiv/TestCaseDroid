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
 * 利用反射机制和JUnit5的API来运行单个测试类
 */
//输入的参数
//依赖引入问题
public class TestRunner {
    public static void main(String[] args) throws Exception {
        TestExecutionSummary summary1= runSingleTest("E:\\Tutorial\\taitest\\target\\test-classes","test.CFGTestTest");
        TestExecutionSummary summary2= runSingleTest("E:\\Tutorial\\TestCaseDroid\\target\\test-classes","TestCaseDroid.config.SootConfigTest");
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
     * 运行单个测试
     * @param classPath 类路径
     * @param className 类名
     * @throws Exception 异常
     */
    private static TestExecutionSummary runSingleTest(String classPath,String className) throws Exception {
        Class<?> testClass = getTestClassFromPath(classPath, className);
        assert testClass != null;
        return runTest(testClass);
    }

    /**
     * 通过类路径和类名获取测试类
     * @param classPath 类路径
     * @param className 类名 eg: com.example.Test
     * @return 测试类
     * @throws Exception 异常
     */
    private static Class<?> getTestClassFromPath(String classPath,String className) throws Exception {
        //查看路径是否存在
        File file = new File(classPath);
        if (!file.exists()) {
            System.out.println("路径不存在");
            return null;
        }
        URL url = file.toURI().toURL();
        try (URLClassLoader classLoader = new URLClassLoader(new URL[]{url})) {
            return classLoader.loadClass(className);
        }catch (ClassNotFoundException e){
            System.out.println("类加载失败"+e.getMessage());
            throw e;
        }catch (Exception e){
            System.out.println("异常"+e.getMessage());
            throw e;
        }
    }

    /**
     * 运行测试
     * @param testClass 测试类
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