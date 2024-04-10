package TestCaseDroid.script;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;


@Setter
@Slf4j
public class TestCaseRunner {
    private static String mavenPath;//maven executable path
    private static String os = System.getProperty("os.name").toLowerCase();


    private static  void compareTestCases(TestCase testCase1, TestCase testCase2) {
        if (testCase1.equals(testCase2)) {
            System.out.println("Test cases are equal");
        } else {
            System.out.println("Test cases are not equal");
        }
    }
    private static void runSingleTest(String projectPath, String testClassName, String mavenPath) {
        //test the maven executable path is valid
        File maven = new File(mavenPath);
        if (!maven.exists() || maven.isDirectory()) {
            log.error("Invalid maven executable path");
            throw new IllegalArgumentException("Invalid maven executable path");
        }
        TestCaseRunner.mavenPath = mavenPath;
        runSingleTest(projectPath, testClassName);
    }
    private static TestCase runSingleTest(String projectPath, String testClassName ,String ...params) {
        //获取系统环境中的Maven路径
        if (TestCaseRunner.mavenPath == null || TestCaseRunner.mavenPath.isEmpty()) {
            mavenPath = System.getenv("MAVEN_HOME");
            if (mavenPath == null) {
                log.error("Maven home or executable not found");
                throw new IllegalArgumentException("Maven home or executable not found");
            }
        }
        //check if the project path is valid
        File project = new File(projectPath);
        if (!project.exists() || !project.isDirectory()) {
            log.error("Invalid project path");
            throw new IllegalArgumentException("Invalid project path");
        }
        try {
            // 构建测试用例对象
            TestCase testCase = new TestCase(projectPath, testClassName,params);
            // 执行测试用例
            ProcessBuilder processBuilder = getProcessBuilder(testCase);
            processBuilder.directory(project);
            processBuilder.redirectErrorStream(true);
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            StringBuilder content = new StringBuilder();
            while ((line =reader.readLine()) != null) {
//                System.out.println(line);
                content.append(line).append("\n");
            }
            process.waitFor();
            parseTestResults(content.toString(), testCase);
            System.out.println(testCase);
            return testCase;
        } catch (Exception e) {
            log.error("Error running test case", e);
        }
        return null;
    }

    private static ProcessBuilder getProcessBuilder(TestCase testCase) {
        return new ProcessBuilder(getMVNCommand(testCase));
    }

    private static List<String> getMVNCommand(TestCase testCase) {
        List<String> command = new ArrayList<>();
        if (os.contains("win")) {
            command.add("cmd");
            command.add("/c");
            command.add("mvn");
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            command.add("sh");
            command.add("-c");
            command.add("mvn");
        } else {
            command.add(mavenPath);
        }
        command.add("test");
        command.add("-Dtest=" + testCase.getTestClassName());
        if (testCase.getParameters() != null && !testCase.getParameters().isEmpty()) {
            command.add("-Dexec.args=");
            command.addAll(testCase.getParameters());
        }
        return command;
    }

    private static void parseTestResults(String content, TestCase testCase) {
        // 解析测试结果
        String line;
        String[] lines = content.split("\n");
        for (String s : lines) {
            line = s;
            if (line.contains("Tests run:")) {
                String[] parts = line.split(",");
                for (String part : parts) {
                    if (part.contains("Tests run:")) {
                        testCase.setTotalTests(Integer.parseInt(part.split(":")[1].trim()));
                    } else if (part.contains("Failures:")) {
                        testCase.setFailedTests(Integer.parseInt(part.split(":")[1].trim()));
                    } else if (part.contains("Errors:")) {
                        testCase.setErrorTests(Integer.parseInt(part.split(":")[1].trim()));
                    } else if (part.contains("Skipped:")) {
                        testCase.setSkippedTests(Integer.parseInt(part.split(":")[1].trim()));
                    }
                }
            } else if (line.contains("BUILD FAILURE")) {
                testCase.setErrorMessage("Build failure");
                log.error("Build failure");
            }
            testCase.setPassedTests(testCase.getTotalTests() - testCase.getFailedTests() - testCase.getErrorTests() - testCase.getSkippedTests());
        }
    }

    public static void main(String[] args) {
        TestCase testCase1 = runSingleTest("E:\\Tutorial\\taitest\\", "test.CFGTestTest");
        TestCase testCase2 = runSingleTest("E:\\Tutorial\\TestCaseDroid\\", "TestCaseDroid.config.SootConfigTest");
        assert testCase1 != null;
        compareTestCases(testCase1, testCase2);
    }
}
