package TestCaseDroid.script;

import lombok.Getter;
import lombok.Setter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Setter
@Getter
public class TestCaseRunner {
    private String project1TestPath = "";//class path of project1 test
    private String project2TestPath = "";//class path of project2 test

    TestCaseRunner(String project1TestPath, String project2TestPath) {
        this.project1TestPath = project1TestPath;
        this.project2TestPath = project2TestPath;
    }
    private String runTestCase(String projectTestPath) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("java", "-cp", projectTestPath, "org.junit.runner.JUnitCore", "TestSuite");
        try {
            Process process = processBuilder.start();
            process.waitFor();
            InputStream is = process.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            StringBuilder result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line).append("\n");
            }
            return result.toString();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void main(String[] args) {
        TestCaseRunner testCaseRunner = new TestCaseRunner("E:\\Tutorial\\TestCaseDroid\\target\\test-classes\\TestCaseDroid\\config\\SootConfigTest.class","");
        String result1 = testCaseRunner.runTestCase(testCaseRunner.getProject1TestPath());
    }

}
