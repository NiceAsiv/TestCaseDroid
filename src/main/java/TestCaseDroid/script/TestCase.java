package TestCaseDroid.script;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Setter
@Getter
public class TestCase {
    private String testClassName;
    private String projectPath;
    private List<String> testMethods;
    private List<String> failedMethods;
    private List<String> parameters;
    private int totalTests;
    private int failedTests;
    private int passedTests;
    private int errorTests;
    private int skippedTests;
    private String errorMessage;

    public TestCase(String projectPath, String testClassName,String ...params) {
        this.projectPath = projectPath;
        this.testClassName = testClassName;
        this.totalTests = 0;
        this.failedTests = 0;
        this.errorTests = 0;
        this.passedTests = 0;
        this.skippedTests = 0;
        this.errorMessage = "";
        this.parameters = new ArrayList<>(params.length);
        this.parameters.addAll(Arrays.asList(params));
    }
    @Override
    public String toString() {
        return "Test Case: " + testClassName + "\n" +
                "Passed Tests: " + passedTests + "\n" +
                "Total Tests: " + totalTests + "\n" +
                "Failed Tests: " + failedTests + "\n" +
                "Skipped Tests: " + skippedTests + "\n" +
                "Error Message: " + errorMessage;
    }
    @Override
    public boolean equals(Object obj) {
        if(obj instanceof TestCase) {
            TestCase other = (TestCase) obj;
            return testClassName.equals(other.testClassName) &&
                    totalTests == other.totalTests &&
                    failedTests == other.failedTests &&
                    passedTests == other.passedTests &&
                    skippedTests == other.skippedTests &&
                    errorMessage.equals(other.errorMessage);
        }
        return false;
    }
}
