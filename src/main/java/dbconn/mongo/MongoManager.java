package dbconn.mongo;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import java.util.Collections;

/**
 * An implementation of the DatabaseManager type for interfacing with MongoDB.
 *
 * @author Max Meinhold <mxmeinhold@gmail.com>
 */
public class MongoManager implements dbconn.DatabaseManager {

    /** The connection object to the MongoDB server*/
    private MongoClient server;


    /**
     * Initiates connection to the server.
     */
    public MongoManager() {
        server = MongoClients.create(defaults.Secrets.MONGO_CONNECT_STRING);
    }


    /**
     * Creates the specified database and adds the specified user as an owner.
     */
    @Override
    public void create(String dbName, String password) {
        // TODO add global accounts by CSH uid and control roles more cleanly.
        MongoDatabase db = server.getDatabase(dbName);
        final BasicDBObject createUserCommand = new BasicDBObject("createUser", dbName).append("pwd", password)
                .append("roles", Collections.singletonList(new BasicDBObject("role", "dbOwner").append("db", dbName)));
        db.runCommand(createUserCommand);
    }


    /**
     * Deletes all users and data in the specified database, in effect deleting the
     * database.
     */
    @Override
    public void delete(String dbName) {
        MongoDatabase db = server.getDatabase(dbName);
        db.runCommand(new BasicDBObject("dropAllUsersFromDatabase", 1));
        db.drop();
    }


    /** Overwrites a user's password without modifying roles. */
    @Override
    public void setPassword(String dbName, String username, String password) {
        MongoDatabase db = server.getDatabase(dbName);
        final BasicDBObject setPasswordCommand = new BasicDBObject("updateUser", username).append("pwd", password);
        db.runCommand(setPasswordCommand);
    }


    /**
     * Closes connections to the database server.
     */
    @Override
    public void close() {
        server.close(); // TODO send shutdown command?
    }
}
