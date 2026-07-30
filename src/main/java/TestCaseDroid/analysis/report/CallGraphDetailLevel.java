package TestCaseDroid.analysis.report;

/**
 * Controls how much evidence is included in a human/AI report.
 */
public enum CallGraphDetailLevel {
    SUMMARY,
    STANDARD,
    DETAILED;

    public static CallGraphDetailLevel parse(String value) {
        return value == null ? STANDARD : valueOf(value.trim().toUpperCase());
    }

    public String cliName() {
        return name().toLowerCase();
    }
}
