package api.model.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * An exception wrapper for Java SQL exceptions.
 *
 * @author Max Meinhold <mxmeinhold@gmail.com>
 */
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class SQLException extends DeadassException {

    public SQLException(java.sql.SQLException e) {
        super(e);
    }


    public SQLException(String message, java.sql.SQLException e) {
        super(message, e);
    }
}
