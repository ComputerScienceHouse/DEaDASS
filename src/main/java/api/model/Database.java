package api.model;

import api.model.exception.NotFoundException;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * A java object representing database information and containing utility methods for processing database queries
 * @author Max Meinhold <mxmeinhold@gmail.com>
 */
public class Database extends DatabaseObject {
    public String name;
    public int pool_id;
    public String poolTitle;
    public String owner;
    public boolean isGroup;
    public String purpose;
    public DatabaseType type;
    public boolean approved;

    public Database() {

    }


    public Database(int pool_id, String db_name, String purpose, DatabaseType type) {
        this.owner = null;
        this.name = db_name;
        this.pool_id = pool_id;
        this.purpose = purpose;
        this.type = type;
        this.approved = false;
    }


    /**
     * Creates a database object representing the input
     * @param db the ResultSet containing a 1 or more databases
     * @return a Database object representing the first row of the ResultSet
     * @throws SQLException if an exception occurs whilst processing the ResultSet
     * @throws NotFoundException if no databases are in the ResultSet
     */
    @Override
    public Database parse(ResultSet db) throws SQLException, NotFoundException {
        if(db.next()) {
            this.name       = db.getString("name");
            this.poolTitle = db.getString("title");
            this.owner      = db.getString("owner");
            this.isGroup = db.getBoolean("is_group");
            this.purpose    = db.getString("purpose");
            this.type       = DatabaseType.fromString(db.getString("type"));
            this.approved   = db.getBoolean("approved");
            return this;
        } else {
            throw new NotFoundException("Database name unrecognised.");
        }
    }
}
