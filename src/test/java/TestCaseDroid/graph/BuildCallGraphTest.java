package TestCaseDroid.graph;

import org.junit.jupiter.api.Test;
import TestCaseDroid.graph.BuildCallGraph.*;

import static org.junit.jupiter.api.Assertions.*;

class BuildCallGraphTest {

    @Test
    void testBuildCallGraphForClass() {
        BuildCallGraph.buildCallGraphForClass();
    }
}