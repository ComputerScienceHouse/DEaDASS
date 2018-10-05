package dbconn;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import dbconn.mongo.MongoManager;
import dbconn.postgres.PostgresManager;
import mail.Mail;
import org.bson.Document;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Filters.eq;

public class ManagerManager {
    private DatabaseManager mongo;
    private DatabaseManager mysql;
    private DatabaseManager postgres;

    private MongoClient dbDBServer;
    private MongoDatabase dbDB;
    private MongoCollection<Document> dbColl;

    private Mail mail;

    public ManagerManager() {
        mongo = new MongoManager();
        postgres = new PostgresManager();

        dbDBServer = MongoClients.create(defaults.Secrets.MANAGER_MONGO_CONNECT_STRING);
        dbDB = dbDBServer.getDatabase("dbDB");
        dbColl = dbDB.getCollection("dbs");

        mail = new Mail();
    }

    public void create(String dbID) {

        Document db = dbColl.find(eq("_id", new ObjectId(dbID))).first();
        if (!db.getString("status").equals("requested"))
            throw new Error("No standing request for new DB, invalid creation request.");

        String type = db.getString("type");
        String dbName = db.getString("name");
        String uid = db.getString("uid");

        // TODO haddock
        String password = "guh";

        db.append("pwd", password);

        switch (type) {
        case "mongo":
            mongo.create(dbName, password);
        case "mysql":
            mysql.create(dbName, password);
        case "postgres":
            postgres.create(dbName, password);
        }

        db.put("status", "created");
        dbColl.findOneAndReplace(eq("_id", new ObjectId(dbID)), db);

        mail.approve(uid, dbName, password);
    }

    public void delete(String dbID) {

        Document db = dbColl.find(eq("_id", new ObjectId(dbID))).first();

        String type = db.getString("type");
        String dbName = db.getString("name");

        switch (type) {
        case "mongo":
            mongo.delete(dbName);
        case "mysql":
            mysql.delete(dbName);
        case "postgres":
            postgres.delete(dbName);
        }

        dbColl.deleteOne(eq("_id", new ObjectId(dbID)));
    }

    public void request(String uid, String name, String purpose, String type) {
        Document db = new Document("uid", uid).append("name", name).append("purpose", purpose).append("type", type)
                .append("status", "requested");
        dbColl.insertOne(db);
        mail.request(uid, purpose, ((ObjectId) db.get("_id")).toHexString());
    }

    public void close() {
        mongo.close();
        mysql.close();
        postgres.close();
        dbDBServer.close(); // TODO send shutdown command?
    }
}
