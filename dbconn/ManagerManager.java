package dbconn;

import org.bson.Document;
import org.bson.types.ObjectId;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import static com.mongodb.client.model.Filters.*;

import dbconn.mongo.MongoManager;
import dbconn.mysql.MysqlManager;
import dbconn.postgres.PostgresManager;
import mail.Mail;

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
        mysql = new MysqlManager();
        postgres = new PostgresManager();
        
        dbDBServer = MongoClients.create(defaults.Secrets.MANAGER_MONGO_CONNECT_STRING);
        dbDB = dbDBServer.getDatabase("dbDB");
        dbColl = dbDB.getCollection("dbs");
        
        mail = new Mail();
    }
    
    public void create(String dbID) {
        
        Document db = dbColl.find(eq("_id", new ObjectId(dbID))).first();
        if(!db.getString("status").equals("requested"))
            throw new Error("No standing request for new DB, invalid creation request.");
        db.replace("status", "created");

        String type = db.getString("type");
        String dbName = db.getString("name");
        String username = db.getString("username");
        String uid = db.getString("uid");
        
        // TODO haddock
        String password = "guh";
        
        db.append("pwd", password);
        dbColl.updateOne(eq("_id", new ObjectId(dbID)), db);
        
        mail.approve(uid, dbName, password);
        
        switch(type) {
        case "mongo":
            mongo.create(dbName, username, password);
        case "mysql":
            mysql.create(dbName, username, password);
        case "postgres":
            postgres.create(dbName, username, password);
        }
    }
    
    public void delete(String dbID) {
        
        Document db = dbColl.find(eq("_id", new ObjectId(dbID))).first();
        
        String type = db.getString("type");
        String dbName = db.getString("name");
        
        dbColl.deleteOne(eq("_id", new ObjectId(dbID)));
                
        switch(type) {
        case "mongo":
            mongo.delete(dbName);
        case "mysql":
            mysql.delete(dbName);
        case "postgres":
            postgres.delete(dbName);
       }
    }
    
    public void request(String uid, String name, String purpose, String type) {
        Document db = new Document("uid", uid).append("name", name).append("purpose", purpose).append("type", type).append("status", "requested");
        dbColl.insertOne(db);
        mail.request(uid, purpose, ((ObjectId)db.get("_id")).toHexString());
    }
    
    public void close() {
        mongo.close();
        mysql.close();
        postgres.close();
        dbDBServer.close(); // TODO send shutdown command?
    }
}
