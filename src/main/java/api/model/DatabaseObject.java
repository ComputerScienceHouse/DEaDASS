package api.model;

import api.model.exception.NotFoundException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Represents a row in the database.
 * @author Max Meinhold <mxmeinhold@gmail.com>
 */
public abstract class DatabaseObject {


    /**
     * Creates a list of DatabaseObjects
     * @param set the ResultSet the rows to be parsed
     * @return A List of DatabaseObjects representing the data in set
     * @throws SQLException if an exception occurs whilst processing the ResultSet
     */
    public ArrayList<DatabaseObject> parseList(ResultSet set) throws SQLException {
        ArrayList<DatabaseObject> list = new ArrayList<DatabaseObject>();
        try {
            while(!set.isAfterLast())
                list.add(this.parse(set));
        } catch (NotFoundException e) {
            // Ignoring, no list to list, so return empty list.
        }
        return list;
    }


    /**
     * Creates a DatabaseObject representing the input
     * @param set the ResultSet containing a 1 or more rows
     * @return a DatabaseObject representing the first row of the ResultSet
     * @throws SQLException if an exception occurs whilst processing the ResultSet
     * @throws NotFoundException if no rows are in the ResultSet
     */
    public abstract DatabaseObject parse(ResultSet set) throws SQLException, NotFoundException;
}
