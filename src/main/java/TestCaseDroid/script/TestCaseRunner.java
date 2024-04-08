package TestCaseDroid.script;

import TestCaseDroid.script.TestCase;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

/**
 * 调用 Maven API 执行指定的测试用例，并从测试结果中提取执行结果
 */
@Setter
@Slf4j
public class TestCaseRunner {
    private static String mavenPath;//maven executable path


    private static  void compareTestCases(TestCase testCase1, TestCase testCase2) {
        if (testCase1.equals(testCase2)) {
            System.out.println("Test cases are equal");
        } else {
            System.out.println("Test cases are not equal");
        }
    }
    private static void runSingleTest(String projectPath, String testClassName, String mavenPath) {
        TestCaseRunner.mavenPath = mavenPath;
        runSingleTest(projectPath, testClassName);
    }
    private static TestCase runSingleTest(String projectPath, String testClassName) {
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
        //check os type
        String os = System.getProperty("os.name").toLowerCase();
        try {
            // 构建测试用例对象
            TestCase testCase = new TestCase(projectPath, testClassName);
            // 执行测试用例
            ProcessBuilder processBuilder = getProcessBuilder(testClassName, os);
            processBuilder.directory(new File(projectPath));
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

    private static ProcessBuilder getProcessBuilder(String testClassName, String os) {
        ProcessBuilder processBuilder;
        if (os.contains("win")) {
            processBuilder = new ProcessBuilder("cmd", "/c", "mvn", "test", "-Dtest=" + testClassName, "-DfailIfNoTests=false");
        } else if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            processBuilder = new ProcessBuilder("sh", "-c", "mvn", "test", "-Dtest=" + testClassName, "-DfailIfNoTests=false");
        } else {
            processBuilder = new ProcessBuilder(mavenPath, "test", "-Dtest=" + testClassName, "-DfailIfNoTests=false");
        }
        return processBuilder;
    }

    private static void parseTestResults(String content, TestCase testCase) {
        // 解析测试结果
        String line;
        String[] lines = content.split("\n");
        for (int i = 0; i < lines.length; i++) {
            line = lines[i];
            if (line.contains("Tests run:")) {
                String[] parts = line.split(",");
                for (String part : parts) {
                    if (part.contains("Tests run:")) {
                        testCase.setTotalTests(Integer.parseInt(part.split(":")[1].trim()));
                    } else if (part.contains("Failures:")) {
                        testCase.setFailedTests(Integer.parseInt(part.split(":")[1].trim()));
                    } else if (part.contains("Errors:")) {
                        testCase.setFailedTests(Integer.parseInt(part.split(":")[1].trim()));
                    } else if (part.contains("Skipped:")) {
                        testCase.setSkippedTests(Integer.parseInt(part.split(":")[1].trim()));
                    }
                }
            }
        }
    }

    public static void main(String[] args) {
        TestCase testCase1 = runSingleTest("E:\\Tutorial\\TestCaseDroid\\", "TestCaseDroid.config.SootConfigTest");
        TestCase testCase2 = runSingleTest("E:\\Tutorial\\TestCaseDroid\\", "TestCaseDroid.config.SootConfigTest");
        assert testCase1 != null;
        compareTestCases(testCase1, testCase2);
    }
}
