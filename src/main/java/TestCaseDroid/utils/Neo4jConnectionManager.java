package TestCaseDroid.utils;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.driver.*;

import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class Neo4jConnectionManager {
    private Driver driver;

    public void connect(String uri, String user, String password) {
        driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
    }

    public void connect() {
        Properties prop = new Properties();
        try(InputStream input = getClass().getClassLoader().getResourceAsStream("neo4j.properties")) {
            if (input == null) {
                log.error("neo4j.properties not found, please create one in src/main/resources");
                throw new Exception("neo4j.properties not found, please create one in src/main/resources");
            }

            //load a properties file from class path, inside static method
            prop.load(input);

            //get the property value and print it out
            String uri = prop.getProperty("uri");
            String user = prop.getProperty("user");
            String password = prop.getProperty("password");

            if (uri == null || user == null || password == null) {
                log.error("uri, user, password must be set in neo4j.properties");
                throw new Exception("uri, user, password must be set in neo4j.properties");
            }
            driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    public Session getSession() {
        return driver.session();
    }

    public void close() {
        driver.close();
    }

    public static void main(String[] args) {
        Neo4jConnectionManager connectionManager = new Neo4jConnectionManager();
        connectionManager.connect();
        connectionManager.close();
    }
}