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


    // TODO consider throwing error if failure occurs
    /**
     * Sets the user's password for the database
     * @param dbName the database the user belongs to
     * @param username the name of the user
     * @param password the password to set
     */
    void setPassword(String dbName, String username, String password);


    /**
     * Closes the connection to the database.
     */
    void close();
}
