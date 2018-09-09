package dbconn;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

import dbconn.mongo.MongoManager;
import dbconn.mysql.MysqlManager;
import dbconn.postgres.PostgresManager;
import mail.Mail;

public class ManagerManager {
    private DatabaseManager mongo;
    private DatabaseManager mysql;
    private DatabaseManager postgres;
    
    private MongoClient dbDB;
    private Mail mail;
    
    public ManagerManager() {
        mongo = new MongoManager();
        mysql = new MysqlManager();
        postgres = new PostgresManager();
        
        dbDB = MongoClients.create(defaults.Secrets.MANAGER_MONGO_CONNECT_STRING);
        mail = new Mail();
    }
    
    public void create(int dbID) {
        // TODO retrieve dbID from dbDB
        String type = null;
        String dbName = null;
        String username = null;
        String uid = null;
        
        // TODO mark db as live in dbDB
        
        // TODO haddock
        String password = "guh";
        
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
    
    public void delete(int dbID) {
        // TODO retrieve dbID from dbDB
        String type = null;
        String dbName = null;
        
        // TODO delete db from dbDB
        
        switch(type) {
        case "mongo":
            mongo.delete(dbName);
        case "mysql":
            mysql.delete(dbName);
        case "postgres":
            postgres.delete(dbName);
       }
    }
    
    public void request(String uid, String purpose, String type) {
        // TODO store uid, type, and purpose in the dbDB
        // Generate a dbID (have dbDB do this)
        int dbID = 0;
        
        mail.request(uid, purpose, dbID);
    }
    
    public void close() {
        mongo.close();
        mysql.close();
        postgres.close();
        dbDB.close(); // TODO send shutdown command?
    }
}
