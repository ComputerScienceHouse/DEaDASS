package dbconn;

public interface DatabaseManager {

    void create(String dbName, String username, String password);
    void delete(String dbName);
    void close();
    
}
