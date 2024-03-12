package TestCaseDroid.config;

import soot.Scene;

import static org.junit.jupiter.api.Assertions.*;

class SootConfigTest {

    private SootConfig sootConfig;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        sootConfig = new SootConfig();
    }
    @org.junit.jupiter.api.AfterEach
    void tearDown() {
        sootConfig = null;
    }

    @org.junit.jupiter.api.Test
    void setCallGraphAlgorithm() {
        sootConfig.setCallGraphAlgorithm("Spark");
        assertEquals("Spark", sootConfig.getCallGraphAlgorithm());
    }


    @org.junit.jupiter.api.Test
    void setupSoot() {
        sootConfig.setupSoot("TestCaseDroid.test.MainCFA", true);
        assertNotNull(Scene.v().getSootClassPath());
        assertNotNull(Scene.v().getMainClass());
        assertNotNull(Scene.v().getActiveHierarchy());
        assertNotNull(Scene.v().getCallGraph());

        //set to false
        sootConfig.setupSoot("TestCaseDroid.test.MainCFA", false);
        assertNotNull(Scene.v().getSootClassPath());
        assertNotNull(Scene.v().getMainClass());
        assertNotNull(Scene.v().getActiveHierarchy());
    }
}