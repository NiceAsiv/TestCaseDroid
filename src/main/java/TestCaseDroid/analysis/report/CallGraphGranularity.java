package TestCaseDroid.analysis.report;

/**
 * The structural level used to present a call graph.
 */
public enum CallGraphGranularity {
    PACKAGE,
    CLASS,
    METHOD,
    CALL_SITE;

    public static CallGraphGranularity parse(String value) {
        if (value == null) {
            return METHOD;
        }
        String normalized = value.trim().replace('-', '_').toUpperCase();
        if ("CALLSITE".equals(normalized)) {
            normalized = "CALL_SITE";
        }
        return valueOf(normalized);
    }

    public String cliName() {
        return name().toLowerCase().replace('_', '-');
    }
}
