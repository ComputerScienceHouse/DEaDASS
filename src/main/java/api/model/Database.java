package api.model;

import api.model.exception.NotFoundException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * A java object representing database information and containing utility methods for processing database queries
 * @author Max Meinhold <mxmeinhold@gmail.com>
 */
public class Database extends DatabaseObject {
    public String name;
    public int pool_id;
    private String pool_title;
    public String owner;
    public boolean is_group;
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
            this.pool_title = db.getString("title");
            this.owner      = db.getString("owner");
            this.is_group   = db.getBoolean("is_group");
            this.purpose    = db.getString("purpose");
            this.type       = DatabaseType.valueOf(db.getString("type"));
            this.approved   = db.getBoolean("approved");
            return this;
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
