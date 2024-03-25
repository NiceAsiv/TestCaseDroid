package TestCaseDroid.utils;
import org.neo4j.driver.Session;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Edge;

public class NodesUploader {
    private Neo4jConnectionManager connectionManager;

    public NodesUploader(Neo4jConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    public void insertCallGraph(CallGraph callGraph) {
        Session session = connectionManager.getSession();
        Iterable<Edge> edges = null;
        for (Edge edge : edges) {
            String srcMethod = edge.src().method().getSignature();
            String tgtMethod = edge.tgt().method().getSignature();
            String query = String.format("MERGE (a:Method {name: '%s'}) MERGE (b:Method {name: '%s'}) MERGE (a)-[:CALLS]->(b)", srcMethod, tgtMethod);
            session.run(query);
        }
    }
}
