package TestCaseDroid.script;

import lombok.Setter;
@Setter
public class TestCase {
    private String testClassName;
    private String projectPath;
    private int totalTests;
    private int failedTests;
    private int passedTests;
    private int skippedTests;
    private String errorMessage;

    public TestCase(String projectPath, String testClassName) {
        this.projectPath = projectPath;
        this.testClassName = testClassName;
        this.totalTests = 0;
        this.failedTests = 0;
        this.passedTests = 0;
        this.skippedTests = 0;
        this.errorMessage = "";
    }
    @Override
    public String toString() {
        return "Test Case: " + testClassName + "\n" +
                "Total Tests: " + totalTests + "\n" +
                "Failed Tests: " + failedTests + "\n" +
                "Passed Tests: " + passedTests + "\n" +
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
