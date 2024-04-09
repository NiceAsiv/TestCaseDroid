package TestCaseDroid.script;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
class TestCaseTest {

    @Test
    void shouldInitializeWithCorrectValuesAndParameters() {
        TestCase testCase = new TestCase("path", "className", "param1", "param2");

        assertEquals("path", testCase.getProjectPath());
        assertEquals("className", testCase.getTestClassName());
        assertEquals(Arrays.asList("param1", "param2"), testCase.getParameters());
    }

    @Test
    void shouldReturnCorrectToStringWithParameters() {
        TestCase testCase = new TestCase("path", "className", "param1", "param2");
        String expected = "Test Case: className\n" +
                "Passed Tests: 0\n" +
                "Total Tests: 0\n" +
                "Failed Tests: 0\n" +
                "Skipped Tests: 0\n" +
                "Error Message: ";
        assertEquals(expected, testCase.toString());
    }

    @Test
    void shouldReturnTrueWhenComparingEqualTestCasesWithParameters() {
        TestCase testCase1 = new TestCase("path", "className", "param1", "param2");
        TestCase testCase2 = new TestCase("path", "className", "param1", "param2");
        assertEquals(testCase1, testCase2);
    }

    @Test
    void shouldReturnFalseWhenComparingDifferentTestCasesWithParameters() {
        TestCase testCase1 = new TestCase("path", "className", "param1", "param2");
        TestCase testCase2 = new TestCase("path2", "className2", "param3", "param4");
        assertNotEquals(testCase1, testCase2);
    }
    @Test
    void shouldInitializeWithCorrectValues() {
        TestCase testCase = new TestCase("path", "className", "param1", "param2");

        assertEquals("path", testCase.getProjectPath());
        assertEquals("className", testCase.getTestClassName());
        assertEquals(0, testCase.getTotalTests());
        assertEquals(0, testCase.getFailedTests());
        assertEquals(0, testCase.getPassedTests());
        assertEquals(0, testCase.getErrorTests());
        assertEquals(0, testCase.getSkippedTests());
        assertEquals("", testCase.getErrorMessage());
        assertEquals(Arrays.asList("param1", "param2"), testCase.getParameters());
    }

    @Test
    void shouldReturnCorrectToString() {
        TestCase testCase = new TestCase("path", "className");
        String expected = "Test Case: className\n" +
                "Passed Tests: 0\n" +
                "Total Tests: 0\n" +
                "Failed Tests: 0\n" +
                "Skipped Tests: 0\n" +
                "Error Message: ";
        assertEquals(expected, testCase.toString());
    }

    @Test
    void shouldReturnTrueWhenComparingEqualTestCases() {
        TestCase testCase1 = new TestCase("path", "className");
        TestCase testCase2 = new TestCase("path", "className");
        assertEquals(testCase1, testCase2);
    }

    @Test
    void shouldReturnFalseWhenComparingDifferentTestCases() {
        TestCase testCase1 = new TestCase("path", "className");
        TestCase testCase2 = new TestCase("path2", "className2");
        assertNotEquals(testCase1, testCase2);
    }
}