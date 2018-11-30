package dbconn;

public interface DatabaseManager {

    void create(String dbName, String password);
    void delete(String dbName);
    void close();
    
}
