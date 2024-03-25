package TestCaseDroid.utils;
import org.neo4j.driver.*;

public class Neo4jConnectionManager {
    private Driver driver;

    public void connect(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    public Session getSession() {
        return driver.session();
    }

    public void close() {
        driver.close();
    }

    public static void main(String[] args) {
        Neo4jConnectionManager connectionManager = new Neo4jConnectionManager();
        connectionManager.connect("bolt://localhost:7687", "neo4j", "abc123");
        connectionManager.close();
    }
}