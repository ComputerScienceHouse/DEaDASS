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
        ObjectId id =  new ObjectId(dbID);
        
        Document db = dbColl.find(eq("_id", id)).first();
        if (!db.getString("status").equals("requested"))
            throw new Error("No standing request for new DB, invalid creation request.");

        String type = db.getString("type");
        String dbName = db.getString("name");
        String uid = db.getString("uid");

        mail.approve(uid, dbName, create(id, db));// TODO don't email passwords
    }
    
    private String create(ObjectId id, Document db) {
        // TODO haddock
        String password = "guh";

        db.append("pwd", password);
        String dbName = db.getString("name");
        
        switch (db.getString("type")) {
        case "mongo":
            mongo.create(dbName, password);
        case "mysql":
            mysql.create(dbName, password);
        case "postgres":
            postgres.create(dbName, password);
        }

        db.put("status", "created");
        dbColl.findOneAndReplace(eq("_id", id), db);
        
        return password;
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

    public String request(String uid, String name, String purpose, String type) {
        String status = "requested";
        Document db = new Document("uid", uid).append("name", name).append("purpose", purpose).append("type", type)
                .append("status", status);
        dbColl.insertOne(db);
        
        ObjectId dbId = (ObjectId) db.get("_id");
        //TODO test if approved.
        if(status.equals("approved"))
            return create(dbId, db);
        
        mail.request(uid, purpose, dbId.toHexString());
        return null;
        
    }

    public void close() {
        mongo.close();
        mysql.close();
        postgres.close();
        dbDBServer.close(); // TODO send shutdown command?
    }
}
