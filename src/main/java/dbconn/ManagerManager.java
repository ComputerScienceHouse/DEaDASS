package dbconn;

import api.model.Database;
import api.model.DatabaseType;
import api.model.Message;
import api.model.exception.NotFoundException;
import dbconn.mongo.MongoManager;
import mail.Mail;
import password.Password;

import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;

import static api.model.DatabaseType.*;

/**
 * The manager driver.
 * Handles all common functionality for managing databases and delegates instance specific commands.
 *
 * @author Max Meinhold <mxmeinhold@gmail.com>
 */
public class ManagerManager {

    /**
     * Get owner, is_group, approved, type, id
     * Param: 1: db Name
     */
    private PreparedStatement getDBAndPoolStmt;
    /**
     * Get owner
     * Param: 1: pool_id
     */
    private PreparedStatement getPoolStmt;
    /**
     * Create new db.
     * Param 1: pool_id, 2: dbname, 3: purpose, 4: type, 5: approved
     */
    private PreparedStatement insertDBStmt;
    /** Get total, limit, owner, is_group. Param 1: pool id */
    private PreparedStatement getCountDBsByPoolStmt;
    /**
     * Create new user.
     * Param 1: database, 2: pool_id, 3: is_group, 4: username, 5:last_reset
     */
    private PreparedStatement insertUserStmt;
    /** Deletes a db. Param 1: dbName */
    private PreparedStatement deleteDBStmt;
    /** Deletes a db's users. Param 1: dbName */
    private PreparedStatement deleteDBUsersStmt;
    /** Approves a database request. Param 1: dbName */
    private PreparedStatement approveStmt;
    /** Resets a user password. Param: 1: date, 2: user */
    private PreparedStatement setPassStmt;
    /**
     * Get a of all databases and their pool info
     * select title, owner, is_group, name, purpose, approved, type
     */
    private PreparedStatement getDBsStmt;

    /** The connection object for the manager's sql db. */
    private Connection managerConnection;

    // Instance managers
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

            String getPoolDbCount = "select count(*) as total, num_limit as limit, owner, is_group "
                    + "from pools, databases "
                    + "where id=pool and id=? and approved "
                    + "group by id ";
            getCountDBsByPoolStmt = managerConnection.prepareStatement(getPoolDbCount);

            String getPool = "select owner "
                    + "from pools "
                    + "where id=?";
            getPoolStmt = managerConnection.prepareStatement(getPool);

            String insertDB = "insert into databases "
                    + "(pool, name, purpose, type, approved) "
                    + "values (?, ?, ?, ?, ?)";
            insertDBStmt = managerConnection.prepareStatement(insertDB);

            String getDbAndPoolByName = "select owner, is_group, approved, type, id "
                    + "from databases, pools "
                    + "where databases.name=? and pool=id";
            getDBAndPoolStmt = managerConnection.prepareStatement(getDbAndPoolByName);

            String insertUser = "insert into users "
                    + "(database, owner, is_group, username, last_reset) "
                    + "values (?, ?, ?, ?, ?)";
            insertUserStmt = managerConnection.prepareStatement(insertUser);

            String deleteDB = "delete from databases where name=?";
            deleteDBStmt = managerConnection.prepareStatement(deleteDB);

            String deleteDBUsers = "delete from users where database=?";
            deleteDBUsersStmt = managerConnection.prepareStatement(deleteDBUsers);

            String approveDB = "update databases set approved=true where name=?";
            approveStmt = managerConnection.prepareStatement(approveDB);

            String setPassword = "update users set last_reset=? where username=? and database=?";
            setPassStmt = managerConnection.prepareStatement(setPassword);

            String getDBs = "select title, owner, is_group, name, purpose, approved, type " +
                    "from databases, pools " +
                    "where pool=id";
            getDBsStmt = managerConnection.prepareStatement(getDBs);

        } catch (SQLException e) {
            // TODO report this in some way? Maybe email someone....
            System.err.println("Manager DB errored while connecting");
            e.printStackTrace();
        }

        mail = new Mail();

        Password.init();
    }


    /**
     * Checks the status of a database
     * @param database the name of the database to check
     * @return a JSON string containing the status of the database.
     */
    public String isPending(String database) {
        String status = "denied/not requested";
        try {
            getDBAndPoolStmt.setString(1, database);
            ResultSet databaseResult = getDBAndPoolStmt.executeQuery();
            if(databaseResult.next()) {
                if (databaseResult.getBoolean("approved"))
                    status = "approved";
                else
                    status = "pending";
            }
            databaseResult.close();
        } catch (SQLException e) {
            e.printStackTrace(); // TODO
        }
        return "{\"status\" : \"" + status + "\"}";
    }


    /**
     * Approves a database, creates it, and notifies the owner.
     * @param dbName the name of the database to be approved
     * @return a Message object containing the result of the operation
     */
    public Message approve(String dbName) {
        // Get the db. Check not approved yet. Set approved. Send email, call normal create.
        try {
            getDBAndPoolStmt.setString(1, dbName);
            ResultSet db = getDBAndPoolStmt.executeQuery();
            if(!db.next())
                return new Message("No DB by that name to approve", Message.Type.ERROR);
            String owner = db.getString("owner");
            if (db.getBoolean("approved"))
                return new Message("Database already approved.", Message.Type.ERROR);

            approveStmt.setString(1, dbName);
            approveStmt.execute();

            create(dbName);
            mail.approve(owner, dbName);
            db.close();

        } catch (SQLException e) {
            e.printStackTrace();
            // TODO specify the type of error? E.g. duplicate names.
            return new Message("SQL error occurred. Try again or check logs.", Message.Type.ERROR);
        }
        return new Message("Database approved.", Message.Type.SUCCESS);
    }


    /**
     * Deletes a standing database request and notifies the requester.
     * @param dbName the database to deny.
     * @return a Message object containing the result of the action.
     */
    public Message deny(String dbName) {
        // Just drop the request.
        try {
            getDBAndPoolStmt.setString(1, dbName);
            ResultSet db = getDBAndPoolStmt.executeQuery();
            if(!db.next())
                return new Message("No DB by that name", Message.Type.ERROR);
            if (!db.getBoolean("approved")) {
                String owner = db.getString("owner");
                mail.deny(owner, dbName);

                // Drop it.
                deleteDBStmt.setString(1, dbName);
                deleteDBStmt.execute();
                return new Message("DB request deleted.", Message.Type.SUCCESS);
            } else
                return new Message("DB not awaiting request.", Message.Type.ERROR);
        } catch (SQLException se) {
            se.printStackTrace();
            return new Message("SQL error. Try again or check logs.", Message.Type.ERROR);
        }
    }


    /**
     * Creates a new database.
     * @param dbName The name of the database to be created
     * @return a Message object containing the result of the operation
     */
    private Message create(String dbName) {
        // Get the db. Check approved. Set a password. Call the child create
        try {
            String password = "";
            getDBAndPoolStmt.setString(1, dbName);
            ResultSet db = getDBAndPoolStmt.executeQuery();
            db.next();
            if(db.getBoolean("approved")) {

                // Define a new user account
                insertUserStmt.setString(1, dbName);
                insertUserStmt.setString(4, dbName);
                insertUserStmt.setInt(2, db.getInt("id"));
                insertUserStmt.setBoolean(3, db.getBoolean("is_group"));
                insertUserStmt.setDate(5, new java.sql.Date(new java.util.Date().getTime()));
                insertUserStmt.execute();

                password = Password.getPassword();

                if(password.equals(""))
                    return new Message("Failed to generate password.", Message.Type.ERROR);

                switch (valueOf(db.getString("type"))) {
                    case MONGO:
                        mongo.create(dbName, password);
                        break;
                    case POSTGRES:
                        break;
                    case MYSQL:
                        break;
                    default:
                        return new Message("Unknown database type", Message.Type.ERROR);
                }
            } else {
                return new Message("Specified database not marked as approved.", Message.Type.ERROR);
            }
            db.close();
            if(!password.equals("") )
                return new Message("password:" + password, Message.Type.SUCCESS);
        } catch (SQLException se) {
            se.printStackTrace();
            // TODO specify the type of error? E.g. duplicate names.
            return new Message("Create SQL error. Please try again or report to an RTP.", Message.Type.ERROR);
        }
        return new Message("Create failed to generate a password.", Message.Type.ERROR);
    }


    /**
     * Deletes a database.
     * @param dbName the name of the database to delete
     * @return a Message object containing the result of the operation
     */
    public Message delete(String dbName) {
        System.out.println(dbName);
        // Get the db. Delete it. Drop the record. Drop its users.
        try {
            getDBAndPoolStmt.setString(1, dbName);
            ResultSet db = getDBAndPoolStmt.executeQuery();

            if(!db.next())
                return new Message("No DB to delete", Message.Type.ERROR);

            switch (DatabaseType.valueOf(db.getString("type"))) {
                case MONGO:
                    mongo.delete(dbName);
                    break;
                case POSTGRES:
                    break;
                case MYSQL:
                    break;
                default:
                    return new Message("Unknown database type", Message.Type.ERROR);
            }

            deleteDBUsersStmt.setString(1, dbName);
            deleteDBUsersStmt.execute();

            deleteDBStmt.setString(1, dbName);
            deleteDBStmt.execute();

        } catch (SQLException e) {
            e.printStackTrace();
            // TODO specify the type of error? E.g. duplicate names.
            return new Message("Delete SQL error. Please try again or report to an RTP.", Message.Type.ERROR);
        }
        return new Message("Database sucessfully deleted.", Message.Type.SUCCESS);
    }


    /**
     * Creates a request for a database. If able to auto approve, also creates the db.
     * @param db the db object to request
     * @return a JSON string indicating the status of the request
     * @throws api.model.exception.SQLException if there is an error in a SQL query
     * @throws NotFoundException if the pool ID is unrecognised.
     */
    public String request(Database db) throws api.model.exception.SQLException, NotFoundException {

        try {
            // Get a count of dbs and check if we should auto approve this request.
            getCountDBsByPoolStmt.setInt(1,db.pool_id);
            ResultSet dbs = getCountDBsByPoolStmt.executeQuery();
            if(dbs.next()) {
                int total_dbs = dbs.getInt("total");
                int limit = dbs.getInt("limit");
                db.approved = (limit < 0) ? true : total_dbs < limit; // If -1, limit is infinity.
                db.owner = dbs.getString("owner");
                db.is_group = dbs.getBoolean("is_group");
            } else {
                getPoolStmt.setInt(1, db.pool_id);
                ResultSet pool = getPoolStmt.executeQuery();
                if(pool.next())
                    db.approved = true;
                else
                    throw new NotFoundException("Pool ID not recognised");
                pool.close();
            }
            dbs.close();

            // Insert the record into the db
            insertDBStmt.setInt(1, db.pool_id);
            insertDBStmt.setString(2, db.name);
            insertDBStmt.setString(3, db.purpose);
            insertDBStmt.setString(4, db.type.toString());
            insertDBStmt.setBoolean(5, db.approved);
            insertDBStmt.execute();

        } catch (SQLException se) {
            // TODO specify the type of error? E.g. duplicate names.
            throw new api.model.exception.SQLException(se);
        }

        if(db.approved)
            return create(db.name);
        mail.request(db.owner, db.purpose, db.name);
        return "{ \"status\" : \"requested\" }";
    }


    /**
     * Resets a users password
     * @param database the database the user belongs to
     * @param username the username of the user
     * @return a Message containing either the password or an error
     */
    public Message setPassword(String database, String username) {
        String password = Password.getPassword();

        try {
            setPassStmt.setDate(1, new Date(new java.util.Date().getTime()));
            setPassStmt.setString(2, username);
            setPassStmt.setString(3, database);
            setPassStmt.execute();

            getDBAndPoolStmt.setString(1, database);
            ResultSet db = getDBAndPoolStmt.executeQuery();
            if(!db.next())
                return new Message("No db found", Message.Type.ERROR);

            switch(DatabaseType.valueOf(db.getString("type"))) {
                case MONGO:
                    mongo.setPassword(database, username, password);
                    break;
                case POSTGRES:
                    break;
                case MYSQL:
                    break;
                default:
                    return new Message("Unknown database type", Message.Type.ERROR);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new Message("Failed to set password.", Message.Type.ERROR);
        }

        return new Message(password, Message.Type.SUCCESS);
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


    /**
     * Gets a list of all databases known to the system
     * @return a list of databases
     * @throws api.model.exception.SQLException if there is an error while processing the Query
     */
    public ArrayList<Database> listDatabases() throws api.model.exception.SQLException {
        try {
            return Database.parseDatabases(getDBsStmt.executeQuery());
        } catch (SQLException e) {
            throw new api.model.exception.SQLException(e);
        }
    }


    /**
     * Gets a specific database
     * @param database the name of the database to find
     * @return the Database object representing the requested database
     * @throws api.model.exception.SQLException if there is an error while processing the Query
     * @throws NotFoundException if the database name is unrecognised.
     */
    public Database getDatabase(String database) throws api.model.exception.SQLException, NotFoundException {
        try {
            this.getDBAndPoolStmt.setString(1, database);
            return Database.parseDatabase(getDBAndPoolStmt.executeQuery());
        } catch (SQLException e) {
            throw new api.model.exception.SQLException(e);
        }
    }


    public String listPools() {
        // TODO get a list of all pools
        return "{ \"message\":\"Not yet implemented.\" }";
    }


    public Message createPool() {
        // TODO
        return new Message("Not yet implemented", Message.Type.ERROR);
    }


    public String getPool(int id) {
        // TODO get info about a specific pool
        return "{ \"message\":\"Not yet implemented.\" }";
    }


    public Message deletePool(int id) {
        // TODO
        return new Message("Not yet implemented", Message.Type.ERROR);
    }


    public String listUsers(String database) {
        // TODO get a list of all users
        return "{ \"message\":\"Not yet implemented.\" }";
    }


    public Message createUser(String database) {
        // TODO
        return new Message("Not yet implemented", Message.Type.ERROR);
    }


    public String getUser(String database, String username) {
        // TODO get info about a specific user
        return "{ \"message\":\"Not yet implemented.\" }";
    }


    public Message deleteUser(String database, String username) {
        // TODO
        return new Message("Not yet implemented", Message.Type.ERROR);
    }


    public String listUsers(int pool) {
        // TODO get a list of all users
        return "{ \"message\":\"Not yet implemented.\" }";
    }


    public Message createUser(int pool) {
        // TODO
        return new Message("Not yet implemented", Message.Type.ERROR);
    }


    public String getUser(int pool, String username) {
        // TODO get info about a specific user
        return "{ \"message\":\"Not yet implemented.\" }";
    }


    public Message deleteUser(int pool, String username) {
        // TODO
        return new Message("Not yet implemented", Message.Type.ERROR);
    }
}
