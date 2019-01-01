package api.model.exception;

/**
 * An exception wrapper for Java SQL exceptions.
 * @author Max Meinhold <mxmeinhold@gmail.com>
 */
public class SQLException extends DeadassException {
    public SQLException(java.sql.SQLException e) {
        super(e);
    }
}
