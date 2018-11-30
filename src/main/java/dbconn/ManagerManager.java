package dbconn;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import dbconn.mongo.MongoManager;
import dbconn.postgres.PostgresManager;
import mail.Mail;
import org.bson.Document;

import static com.mongodb.client.model.Filters.eq;

public class ManagerManager {
    private DatabaseManager mongo;
    private DatabaseManager mysql;
    private DatabaseManager postgres;

    private MongoClient dbDBServer;
    private MongoCollection<Document> dbColl;
    private MongoCollection<Document> userColl;

    private Mail mail;

    public ManagerManager() {
        mongo = new MongoManager();
        postgres = new PostgresManager();

        dbDBServer = MongoClients.create(defaults.Secrets.MANAGER_MONGO_CONNECT_STRING);
        MongoDatabase dbDB = dbDBServer.getDatabase("dbDB");
        dbColl = dbDB.getCollection("dbs");
        userColl = dbDB.getCollection("users");

        mail = new Mail();
    }

    /**
     * Create from an rtp approval and send the email.
     * @param dbName the name of the database to be created.
     */
    public void create(String dbName) {

        Document db = dbColl.find(eq("name", dbName)).first();
        if (!db.getString("status").equals("requested"))
            throw new Error("No standing request for new DB, invalid creation request.");

        String uid = db.getString("uid");

        mail.approve(uid, dbName, create(db));// TODO don't email passwords
    }

    private String create(Document db) {
        // TODO haddock
        String password = "guh";

        db.append("pwd", password);
        String dbName = db.getString("name");

        // Track number of dbs owned by this user
        String uid = db.getString("uid");
        Document user = userColl.find(eq("uid", uid)).first();
        user.put("numDbs", 1 + user.getInteger("numDbs"));
        userColl.updateOne(eq("uid", uid), user);


        switch (db.getString("type")) {
        case "mongo":
            mongo.create(dbName, password);
        case "mysql":
            mysql.create(dbName, password);
        case "postgres":
            postgres.create(dbName, password);
        }

        db.put("status", "created");
        dbColl.findOneAndReplace(eq("name", dbName), db);

        return password;
    }

    public void delete(String dbName) {

        Document db = dbColl.find(eq("name", dbName)).first();

        // Track number of dbs owned by this user
        String uid = db.getString("uid");
        Document user = userColl.find(eq("uid", uid)).first();
        user.put("numDbs", 1 - user.getInteger("numDbs"));
        userColl.updateOne(eq("uid", uid), user);

        String type = db.getString("type");

        switch (type) {
        case "mongo":
            mongo.delete(dbName);
        case "mysql":
            mysql.delete(dbName);
        case "postgres":
            postgres.delete(dbName);
        }

        dbColl.deleteOne(eq("name", dbName));
    }

    public String request(String uid, String name, String purpose, String type) {
        // DB names are unique, so if exists, error.
        if(dbColl.find(new Document("name", name)).first() != null)
            throw new Error("Database name already taken.");

        String status = "requested";

        // Check the user's permissions. If the user is at or above db limit, request, else create
        Document user = userColl.find(eq("uid", uid)).first();
        if(user == null) {
            user = new Document("uid", uid).append("numDbs", 0).append("dbLimit", defaults.Secrets.DEFAULT_LIMIT);
            userColl.insertOne(user);
        }

        if(user.getInteger("numDbs") < user.getInteger("dbLimit"))
            status = "approved";


        // Generate a new entry.
        Document db = new Document("uid", uid).append("name", name).append("purpose", purpose).append("type", type)
                .append("status", status);
        dbColl.insertOne(db);

        if(status.equals("approved"))
            return create(db);

        mail.request(uid, purpose, name);
        return "";
    }

    public void close() {
        mongo.close();
        mysql.close();
        postgres.close();
        dbDBServer.close(); // TODO send shutdown command?
    }
}
