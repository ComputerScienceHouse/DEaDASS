package dbconn;

/**
 * A DatabaseManager provides methods for the Manager Manager to delegate database specific tasks away.
 *
 * @author Max Meinhold <mxmeinhold@gmail.com>
 */
public interface DatabaseManager {

    /**
     * Creates a new database and database user for that database.
     * @param dbName the name of the database and the user's password
     * @param password the user's password
     */
    void create(String dbName, String password);


    /**
     * Deletes a database.
     * @param dbName the name of the database to delete.
     */
    void delete(String dbName);


    /**
     * Closes the connection to the database.
     */
    void close();
    
}
