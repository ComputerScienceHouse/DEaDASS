package dbconn;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import dbconn.mongo.MongoManager;
import mail.Mail;
import org.bson.Document;

import java.sql.*;
import java.util.Properties;

import static com.mongodb.client.model.Filters.eq;

public class ManagerManager {
    private PreparedStatement getDBAndPoolStmt;
    private PreparedStatement insertDBStmt;
    private PreparedStatement getCountDBsByPoolStmt;
    private PreparedStatement insertUserStmt;

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

            String getDbAndPoolByName = "select owner, is_group"
                    + "from databases, pools"
                    + "where name=? and pool=id";
            getDBAndPoolStmt = managerConnection.prepareStatement(getDbAndPoolByName);

            String insertUser = "insert into users "
                    + "(database, owner, is_group, username, last_reset) "
                    + "values (?, ?, ?, ?, ?)";
            this.insertUserStmt = managerConnection.prepareStatement(insertUser);

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
    public void mailCreate(String dbName) {
        // Get the db. Check not approved yet. Set approved. Send email, call normal create.
        String uid = ""; // TODO get info from the db
        create(dbName); // TODO handle the exception and probably dump the password
        String password = "";
        mail.approve(uid, dbName, password);// TODO don't email passwords
    }

    // TODO throwing generic exception is bad....
    private String create(String dbName) throws Exception {
        // Get the db. Check approved. Set a password. Call the child create
        try {
            getDBAndPoolStmt.setString(1, dbName);
            ResultSet db = getDBAndPoolStmt.executeQuery();
            if(db.getBoolean("approved")) {

                // Define a new user account
                this.insertDBStmt.setString(1, dbName);
                this.insertDBStmt.setString(4, dbName);
                this.insertDBStmt.setString(2, db.getString("owner"));
                this.insertDBStmt.setBoolean(3, db.getBoolean("is_group"));
                this.insertDBStmt.setDate(5, new java.sql.Date(new java.util.Date().getTime()));
                this.insertDBStmt.execute();

                // TODO Haddock
                String password = "guh";
                switch (db.getInt("type")) {
                    case 0: // TODO enum.
                        // Mongo
                        this.mongo.create(dbName, password);
                        break;
                    case 1:
                        // Postgres
                        break;
                    case 2:
                        // MySQL
                        break;
                    default:
                        throw new Exception("Well that's not a standard type of database.");
                }
                // TODO return password as some kinda response object so we can track response types and be fancy
                // Oh, but wait until after you close the result set, or drop it in the finally...
            } else {
                throw new Exception("This database isn't marked as approved."
                        + " Something didn't happen in the right order.");
            }
            db.close();
        } catch (SQLException se) {
            se.printStackTrace();
            // TODO do something
        }

        return ""; // TODO
    }

    // TODO redo for postgres
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

    // TODO redo for postgres
    public void close() {
        mongo.close();
//        mysql.close();
//        postgres.close();
        dbDBServer.close(); // TODO send shutdown command?
    }
}
