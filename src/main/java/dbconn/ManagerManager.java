package dbconn;

import api.model.*;
import api.model.exception.BadRequestException;
import api.model.exception.NotFoundException;
import dbconn.mongo.MongoManager;
import mail.Mail;
import password.Password;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


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
    private PreparedStatement getPoolsStmt;

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

            getPoolsStmt  = managerConnection.prepareStatement("select * from pools");

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
     * @throws api.model.exception.SQLException if there is an exception while processing the query
     */
    public Map<String, Object> isPending(String database) throws api.model.exception.SQLException {
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
            throw new api.model.exception.SQLException(e);
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("status", status);
        return map;
    }


    /**
     * Approves a database, creates it, and notifies the owner.
     * @param dbName the name of the database to be approved
     * @return a JSON string containing the result of the operation
     * @throws BadRequestException if the request is invalid
     * @throws api.model.exception.SQLException if there is an exception while accessing the management database
     * @throws NotFoundException if there is no database to approve
     */
    public Map<String, Object> approve(String dbName) throws BadRequestException, api.model.exception.SQLException, NotFoundException {
        // Get the db. Check not approved yet. Set approved. Send email, call normal create.
        try {
            getDBAndPoolStmt.setString(1, dbName);
            ResultSet db = getDBAndPoolStmt.executeQuery();
            if(!db.next())
                throw new NotFoundException("Database name unrecognised");
            String owner = db.getString("owner");
            if (db.getBoolean("approved"))
                throw new BadRequestException("Database already approved");

            approveStmt.setString(1, dbName);
            approveStmt.execute();

            create(dbName);
            mail.approve(owner, dbName);
            db.close();

        } catch (SQLException e) {
            // TODO specify the type of error? E.g. duplicate names.
            throw new api.model.exception.SQLException(e);
        }
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("status", "created");
        return map;
    }


    /**
     * Deletes a standing database request and notifies the requester.
     * @param dbName the database to deny.
     * @return a JSON string containing the result of the action.
     * @throws BadRequestException if the request is invalid
     * @throws api.model.exception.SQLException if there is an exception while accessing the management database
     * @throws NotFoundException if there is no database to deny
     */
    public Map<String, Object> deny(String dbName) throws NotFoundException, BadRequestException, api.model.exception.SQLException {
        // Just drop the request.
        try {
            getDBAndPoolStmt.setString(1, dbName);
            ResultSet db = getDBAndPoolStmt.executeQuery();
            if(!db.next())
                throw new NotFoundException("Database name unrecognised");
            if (!db.getBoolean("approved")) {
                String owner = db.getString("owner");
                mail.deny(owner, dbName);

                // Drop it.
                deleteDBStmt.setString(1, dbName);
                deleteDBStmt.execute();
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("status", "deleted");
                return map;
            } else
                throw new BadRequestException("Database not awaiting request");
        } catch (SQLException se) {
            throw new api.model.exception.SQLException(se);
        }
    }


    /**
     * Creates a new database.
     * @param dbName The name of the database to be created
     * @return a JSON string containing the result of the operation
     * @throws BadRequestException if the request is invalid
     * @throws api.model.exception.SQLException if there is an exception while accessing the management database
     * @throws NotFoundException if the database type is not recognised
     */
    private Map<String, Object> create(String dbName) throws api.model.exception.SQLException, NotFoundException, BadRequestException {
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

                switch (DatabaseType.fromString(db.getString("type"))) {
                    case MONGO:
                        mongo.create(dbName, password);
                        break;
                    case POSTGRES:
                        break;
                    case MYSQL:
                        break;
                    default:
                        throw new NotFoundException("Unrecognised database type");
                }
            } else {
                throw new BadRequestException("Specified database not marked as approved");
            }
            db.close();
            Map<String, Object> returnMapping = new HashMap<String, Object>();
            returnMapping.put("status", "created");
            returnMapping.put("password", password);
            return returnMapping;
        } catch (SQLException se) {
            throw new api.model.exception.SQLException(se);
            // TODO specify the type of error? E.g. duplicate names.
        }
    }


    /**
     * Deletes a database.
     * @param dbName the name of the database to delete
     * @return a JSON string containing the result of the operation
     * @throws api.model.exception.SQLException if there is an exception while accessing the management database
     * @throws NotFoundException if the database type is not recognised or no database is found
     */
    public Map<String, Object> delete(String dbName) throws NotFoundException, api.model.exception.SQLException {
        // Get the db. Delete it. Drop the record. Drop its users.
        try {
            getDBAndPoolStmt.setString(1, dbName);
            ResultSet db = getDBAndPoolStmt.executeQuery();

            if(!db.next())
                throw new NotFoundException("No database to delete");

            switch (DatabaseType.fromString(db.getString("type"))) {
                case MONGO:
                    mongo.delete(dbName);
                    break;
                case POSTGRES:
                    break;
                case MYSQL:
                    break;
                default:
                    throw new NotFoundException("Unrecognised database type");
            }

            deleteDBUsersStmt.setString(1, dbName);
            deleteDBUsersStmt.execute();

            deleteDBStmt.setString(1, dbName);
            deleteDBStmt.execute();

        } catch (SQLException e) {
            throw new api.model.exception.SQLException(e);
            // TODO specify the type of error? E.g. duplicate names.
        }
        Map <String, Object> map = new HashMap<String, Object>();
        map.put("status", "deleted");
        return map;
    }


    /**
     * Creates a request for a database. If able to auto approve, also creates the db.
     * @param db the db object to request
     * @return a JSON string indicating the status of the request
     * @throws api.model.exception.SQLException if there is an error in a SQL query
     * @throws NotFoundException if the pool ID is unrecognised.
     */
    public Map<String, Object> request(Database db) throws api.model.exception.SQLException, NotFoundException, BadRequestException {

        try {
            // Get a count of dbs and check if we should auto approve this request.
            getCountDBsByPoolStmt.setInt(1,db.poolID);
            ResultSet dbs = getCountDBsByPoolStmt.executeQuery();
            if(dbs.next()) {
                int total_dbs = dbs.getInt("total");
                int limit = dbs.getInt("limit");
                db.approved = (limit < 0) ? true : total_dbs < limit; // If -1, limit is infinity.
                db.owner = dbs.getString("owner");
                db.isGroup = dbs.getBoolean("is_group");
            } else {
                getPoolStmt.setInt(1, db.poolID);
                ResultSet pool = getPoolStmt.executeQuery();
                if(pool.next())
                    db.approved = true;
                else
                    throw new NotFoundException("Pool ID not recognised");
                pool.close();
            }
            dbs.close();

            // Insert the record into the db
            insertDBStmt.setInt(1, db.poolID);
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
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("status", "requested");
        return map;
    }


    /**
     * Resets a users password
     * @param database the database the user belongs to
     * @param username the username of the user
     * @return the password
     * @throws api.model.exception.SQLException if the password cann
     * @throws NotFoundException if the database type is not recognised or the database cannot be found
     */
    public String setPassword(String database, String username) throws NotFoundException, api.model.exception.SQLException {
        String password = Password.getPassword();

        try {
            setPassStmt.setDate(1, new Date(new java.util.Date().getTime()));
            setPassStmt.setString(2, username);
            setPassStmt.setString(3, database);
            setPassStmt.execute();

            getDBAndPoolStmt.setString(1, database);
            ResultSet db = getDBAndPoolStmt.executeQuery();
            if(!db.next())
                throw new NotFoundException("Unrecognised database");

            switch(DatabaseType.fromString(db.getString("type"))) {
                case MONGO:
                    mongo.setPassword(database, username, password);
                    break;
                case POSTGRES:
                    break;
                case MYSQL:
                    break;
                default:
                    throw new NotFoundException("Unrecognised database type");
            }
        } catch (SQLException e) {
            throw new api.model.exception.SQLException("Failed to set password", e);
        }

        return password;
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
    public ArrayList<DatabaseObject> listDatabases() throws api.model.exception.SQLException {
        try {
            return new Database().parseList(getDBsStmt.executeQuery());
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
            return new Database().parse(getDBAndPoolStmt.executeQuery());
        } catch (SQLException e) {
            throw new api.model.exception.SQLException(e);
        }
    }


    /**
     * Gets a list of all pools known to the system
     * @return the list of pools
     * @throws api.model.exception.SQLException if there is an error while processing the query
     */
    public ArrayList<DatabaseObject> listPools() throws api.model.exception.SQLException {
        try {
            return new Pool().parseList(getPoolsStmt.executeQuery());
        } catch (SQLException e) {
            throw new api.model.exception.SQLException(e);
        }
    }


    /**
     * Gets a specific pool
     * @param id the id of the pool to find
     * @return the Database object representing the requested pool
     * @throws api.model.exception.SQLException if there is an error while processing the Query
     * @throws NotFoundException if the pool id is unrecognised.
     */
    public DatabaseObject getPool(int id) throws NotFoundException, api.model.exception.SQLException {
        try {
            getPoolStmt.setInt(1, id);
            return new Pool().parse(getPoolStmt.executeQuery());
        } catch (SQLException e) {
            throw new api.model.exception.SQLException(e);
        }
    }
}
