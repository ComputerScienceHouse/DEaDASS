package dbconn;

import dbconn.mongo.MongoManager;
import mail.Mail;

import java.sql.*;
import java.util.Properties;


public class ManagerManager {
    private PreparedStatement getDBAndPoolStmt;
    private PreparedStatement insertDBStmt;
    private PreparedStatement getCountDBsByPoolStmt;
    private PreparedStatement insertUserStmt;
    private PreparedStatement deleteDBStmt;
    private PreparedStatement deleteDBUsersStmt;

    private Connection managerConnection;
    private DatabaseManager mongo;
//    private DatabaseManager mysql;
//    private DatabaseManager postgres;

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

            String getDbAndPoolByName = "select owner, is_group, approved, type"
                    + "from databases, pools"
                    + "where name=? and pool=id";
            getDBAndPoolStmt = managerConnection.prepareStatement(getDbAndPoolByName);

            String insertUser = "insert into users "
                    + "(database, owner, is_group, username, last_reset) "
                    + "values (?, ?, ?, ?, ?)";
            insertUserStmt = managerConnection.prepareStatement(insertUser);

            String deleteDB = "delete from databases where name=?";
            deleteDBStmt = managerConnection.prepareStatement(deleteDB);

            String deleteDBUsers = "delete from users where database=?";
            deleteDBUsersStmt = managerConnection.prepareStatement(deleteDBUsers);

        } catch (SQLException e) {
            // TODO report this in some way? Maybe email someone....
            System.err.println("Manager DB errored while connecting");
            e.printStackTrace();
        }

        mail = new Mail();
    }


    /**
     * Approves a database, creates it, and notifies the owner.
     * @param dbName the name of the database to be approved
     * @throws Exception if the database is already approved TODO this should be a return message type
     */
    public void approve(String dbName) throws Exception {
        // Get the db. Check not approved yet. Set approved. Send email, call normal create.
        try {
            getDBAndPoolStmt.setString(1, dbName);
            ResultSet db = getDBAndPoolStmt.executeQuery();
            String owner = db.getString("owner");
            if (db.getBoolean("approved"))
                throw new Exception("Database already approved.");

            create(dbName);

            mail.approve(owner, dbName);

            db.close();

        } catch (SQLException e) {
            e.printStackTrace();
            // TODO parse errors?
        }
    }


    /**
     * Creates a new database.
     * @param dbName The name of the database to be created
     * @return The password of the newly created user
     * @throws Exception if the db type is unrecognised or the database isn't approved TODO this should be a return with a message
     */
    private String create(String dbName) throws Exception {
        // Get the db. Check approved. Set a password. Call the child create
        try {
            getDBAndPoolStmt.setString(1, dbName);
            ResultSet db = getDBAndPoolStmt.executeQuery();
            if(db.getBoolean("approved")) {

                // Define a new user account
                insertDBStmt.setString(1, dbName);
                insertDBStmt.setString(4, dbName);
                insertDBStmt.setString(2, db.getString("owner"));
                insertDBStmt.setBoolean(3, db.getBoolean("is_group"));
                insertDBStmt.setDate(5, new java.sql.Date(new java.util.Date().getTime()));
                insertDBStmt.execute();

                // TODO Haddock
                String password = "guh";
                // TODO enum.
                switch (db.getInt("type")) {
                    case 0: // Mongo
                        mongo.create(dbName, password);
                        break;
                    case 1: // Postgres
                        break;
                    case 2: // MySQL
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


    /**
     * Deletes a database.
     * @param dbName the name of the database to delete
     * @throws Exception if the db type is unrecognised TODO this should be a return object
     */
    public void delete(String dbName) throws Exception {
        // Get the db. Delete it. Drop the record. Drop its users.
        try {
            getDBAndPoolStmt.setString(1, dbName);
            ResultSet db = getDBAndPoolStmt.executeQuery();

            // TODO enum.
            switch (db.getInt("type")) {
                case 0: // Mongo
                    mongo.delete(dbName);
                    break;
                case 1: // Postgres
                    break;
                case 2: // MySQL
                    break;
                default:
                    throw new Exception("Well that's not a standard type of database.");
            }

            deleteDBStmt.setString(1, dbName);
            deleteDBStmt.execute();

            deleteDBUsersStmt.setString(1, dbName);
            deleteDBUsersStmt.execute();

        } catch (SQLException e) {
            e.printStackTrace();
            // TODO parse exceptions?
        }
        // TODO return a success message
    }


    /**
     * Creates a request for a database. If able to auto approve, also creates the db.
     * @param poolID the id number of the pool this belongs to
     * @param name the name of the new db
     * @param purpose a description of what the db is for
     * @param type the type of db. 0 for mongo, 1 for postgress, 2 for mysql. TODO replace with an enum
     * @return TODO an object containing the status of the result of the action and a message
     * @throws Exception TODO throws an exception if the sql was bad or if create errored. Replace with a return.
     */
    public String request(int poolID, String name, String purpose, int type) throws Exception {

        Boolean approved = false;
        String owner = null;

        try {
            // Get a count of dbs and check if we should auto approve this request.
            getCountDBsByPoolStmt.setInt(1,poolID);
            ResultSet dbs = getCountDBsByPoolStmt.executeQuery();
            int total_dbs = dbs.getInt("total");
            int limit = dbs.getInt("limit");
            approved = (limit < 0)? true : total_dbs < limit; // If -1, limit is infinity.
            owner = dbs.getString("owner");
            dbs.close();

            // Insert the record into the db
            insertDBStmt.setInt(1, poolID);
            insertDBStmt.setString(2, name);
            insertDBStmt.setString(3, purpose);
            insertDBStmt.setInt(4, type);
            insertDBStmt.setBoolean(5, approved);
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


    /**
     * Closes DEaDASS by calling close all of the database connections.
     */
    public void close() {
        try {
            mongo.close();
//            mysql.close();
//            postgres.close();
            managerConnection.close();
        } catch (Exception e) {
            // Ignore, we are shutting down.
        }
    }
}
