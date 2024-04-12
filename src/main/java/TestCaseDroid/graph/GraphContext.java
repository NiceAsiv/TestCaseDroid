package TestCaseDroid.graph;

import lombok.Getter;
import lombok.Setter;



@Setter
@Getter
public class GraphContext {
    private static String targetClassName = "TestCaseDroid.test.CallGraphs";
    private static String entryMethod = "main";
    private static String graphAlgorithm="CHA";
    private static String classesPath;
}
