package dbconn.postgres;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import dbconn.DatabaseManager;

public class PostgresManager implements DatabaseManager {

    private Connection conn;

    private static final String createStatement = "CREATE ROLE ? WITH LOGIN PASSWORD ? "; // TODO add parameters?
    private static final String deleteStatement = "DROP ROLE IF EXISTS ?";

    private PreparedStatement preppedCreate;
    private PreparedStatement preppedDelete;

    /**
     * Initialises a new PostgresManager
     */
    public PostgresManager() {
        String url = defaults.Secrets.POSTGRESS_CONNECT_STRING;
        Properties props = new Properties();
        props.setProperty("user", defaults.Secrets.POSTGRESS_USER);
        props.setProperty("password", defaults.Secrets.POSTGRESS_PASSWORD);
        props.setProperty("ssl", "true");
        try {
            conn = DriverManager.getConnection(url, props);
        } catch (SQLException e) {
            // TODO report this in some way? Maybe email someone....
            System.err.println("Postgress errored while connecting");
            e.printStackTrace();
        }

        try {
            preppedCreate = conn.prepareStatement(createStatement);
            preppedDelete = conn.prepareStatement(deleteStatement);
        } catch (SQLException e) {
            System.err.println("Some error occured in creating postgresql prepared statements.");
            e.printStackTrace();
        }

    }

    @Override
    public void create(String dbName, String password) {
        try {
            preppedCreate.setString(1, dbName);
            preppedCreate.setString(2, password);
            preppedCreate.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void delete(String dbName) {
        try {
            preppedDelete.setString(1, dbName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
