package dbconn.mongo;

import java.util.Collections;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoManager implements dbconn.DatabaseManager {

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
    public void create(String dbName, String username, String password) {
        // TODO add global accounts by CSH uid and control roles more cleanly.
        System.out.println("name: " + dbName + " uid: " + username + " pwd: " + password);
        MongoDatabase db = server.getDatabase(dbName);
        final BasicDBObject createUserCommand = new BasicDBObject("createUser", username).append("pwd", password)
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

    /**
     * Closes connections to the database server.
     */
    @Override
    public void close() {
        server.close(); // TODO send shutdown command?
    }

}
