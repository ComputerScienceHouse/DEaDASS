package dbconn;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import dbconn.mongo.MongoManager;
import defaults.Secrets;
import mail.Mail;
import org.bson.Document;

import java.sql.*;
import java.util.Properties;

import static com.mongodb.client.model.Filters.eq;

public class ManagerManager {
    private PreparedStatement insertDBStmt;
    private PreparedStatement getCountDBsByPoolStmt;

    private Connection managerConnection;
    private DatabaseManager mongo;
//    private DatabaseManager mysql;
//    private DatabaseManager postgres;

    private MongoClient dbDBServer;
    private MongoCollection<Document> dbColl;
    private MongoCollection<Document> userColl;

    private Mail mail;

    public ManagerManager() {
        mongo = new MongoManager();
//        postgres = new PostgresManager();


        Properties props = new Properties();
        props.setProperty("user", defaults.Secrets.MANAGER_USER);
        props.setProperty("password", defaults.Secrets.MANAGER_PASSWORD);
        props.setProperty("ssl", "true");

        // Connect to the database and prepare statements.
        try {
            managerConnection = DriverManager.getConnection(defaults.Secrets.MANAGER_CONNECT_STRING, props);

            String getPoolDbCount = "select count(*) as total, num_limit as limit, owner "
                    + "from pools, databases "
                    + "where id=pool and id=? and approved "
                    + "group by id ";
            getCountDBsByPoolStmt = managerConnection.prepareStatement(getPoolDbCount);

            String insertDB = "insert into databases "
                    + "(pool, name, purpose, type, approved) "
                    + "values (?, ?, ?, ?, ?)";
            insertDBStmt = managerConnection.prepareStatement(insertDB);

        } catch (SQLException e) {
            // TODO report this in some way? Maybe email someone....
            System.err.println("Manager DB errored while connecting");
            e.printStackTrace();
        }

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
//        case "mysql":
//            mysql.create(dbName, password);
//        case "postgres":
//            postgres.create(dbName, password);
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
//        case "mysql":
//            mysql.delete(dbName);
//        case "postgres":
//            postgres.delete(dbName);
        }

        dbColl.deleteOne(eq("name", dbName));
    }

    // TODO throwing generic Exception is bad.
    public String request(int poolID, String name, String purpose, int type) throws Exception {

        // Uniqueness will be handled by the database.

        Boolean approved = false;
        String owner = null;

        // Get the pool from the database, check if at limit.
        // If at or above limit, request. If not, approve. Limit == -1 if unlimited.
        // Add the request info to the db.
        try {
            // Get a count of dbs and check if we should auto approve this request.
            this.getCountDBsByPoolStmt.setInt(1,poolID);
            ResultSet dbs = getCountDBsByPoolStmt.executeQuery();
            int total_dbs = dbs.getInt("total");
            int limit = dbs.getInt("limit");
            approved = (limit < 0)? true : total_dbs < limit; // If -1, limit is infinity.
            owner = dbs.getString("owner");
            dbs.close();

            // Insert the record into the db
            this.insertDBStmt.setInt(1, poolID);
            this.insertDBStmt.setString(2, name);
            this.insertDBStmt.setString(3, purpose);
            this.insertDBStmt.setInt(4, type);
            this.insertDBStmt.setBoolean(5, approved);
            insertDBStmt.execute();

        } catch (SQLException se) {
            se.printStackTrace();
            // TODO specify what the error was.
            throw new Exception("There was some exception in request sql. Not sure what. TODO: parse exceptions");
        }

        if(approved)
            return create(name);
        mail.request(owner, purpose, name);
        return ""; // TODO
    }

    public void close() {
        mongo.close();
//        mysql.close();
//        postgres.close();
        dbDBServer.close(); // TODO send shutdown command?
    }
}
