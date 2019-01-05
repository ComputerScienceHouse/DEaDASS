package api.model;

import api.model.exception.NotFoundException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * A java object representing database information and containing utility methods for processing database queries
 * @author Max Meinhold <mxmeinhold@gmail.com>
 */
public class Database implements JSONUtils.JSONable {
    String name;
    String pool_title;
    String owner;
    boolean is_group;
    String purpose;
    DatabaseType type;
    boolean approved;

    /**
     * Creates a list of database objects
     * @param dbs the ResultSet containing all databases
     * @return A list of database objects representing the date in dbs
     * @throws SQLException if an exception occurs whilst processing the ResultSet
     */
    public static ArrayList<Database> parseDatabases(ResultSet dbs) throws SQLException {
        ArrayList<Database> databases = new ArrayList<Database>();
        try {
            while(!dbs.isAfterLast())
                databases.add(parseDatabase(dbs));
        } catch (NotFoundException e) {
            // Ignoring, no databases to list, so return empty list.
        }
        return databases;
    }


    /**
     * Creates a database object representing the input
     * @param db the ResultSet containing a 1 or more databases
     * @return a Database object representing the first row of the ResultSet
     * @throws SQLException if an exception occurs whilst processing the ResultSet
     * @throws NotFoundException if no databases are in the ResultSet
     */
    public static Database parseDatabase(ResultSet db) throws SQLException, NotFoundException {
        if(db.next()) {
            Database database = new Database();
            database.name       = db.getString("name");
            database.pool_title = db.getString("title");
            database.owner      = db.getString("owner");
            database.is_group   = db.getBoolean("is_group");
            database.purpose    = db.getString("purpose");
            database.type       = DatabaseType.valueOf(db.getString("type"));
            database.approved   = db.getBoolean("approved");
            return database;
        } else {
            throw new NotFoundException("Database name unrecognised.");
        }
    }


    @Override
    public String asJSON() {
        return String.format("{ \"name\" : \"%s\", \"pool\" : \"%s\", \"owner\" : \"%s\", \"is_group\" : \"%b\", "
                        + "\"purpose\" : \"%s\", \"type\" : \"%s\", \"approved\" : \"%b\" }",
                this.name, this.pool_title, this.owner, this.is_group, this.purpose, this.type.toString(), this.approved);
    }
}
