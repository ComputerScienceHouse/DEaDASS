package api.model;

import api.model.exception.NotFoundException;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Pool extends DatabaseObject {

    public String owner;
    public boolean is_group;
    public int limit;
    public int id;
    public String title;


    public Pool() {

    }


    public Pool(String owner, boolean is_group, int num_limit, int id, String title) {
        this.owner = owner;
        this.is_group = is_group;
        this.limit = num_limit;
        this.id = id;
        this.title = title;
    }


    /**
     * Creates a DatabaseObject representing the input
     *
     * @param set the ResultSet containing a 1 or more rows
     * @return a DatabaseObject representing the first row of the ResultSet
     * @throws SQLException      if an exception occurs whilst processing the ResultSet
     * @throws NotFoundException if no rows are in the ResultSet
     */
    @Override
    public Pool parse(ResultSet set) throws SQLException, NotFoundException {
        if(set.next()) {
            this.owner = set.getString("owner");
            this.is_group = set.getBoolean("is_group");
            this.limit = set.getInt("num_limit");
            this.id = set.getInt("id");
            this.title = set.getString("title");
            return this;
        } else
            throw new NotFoundException("No pool found");
    }
}
