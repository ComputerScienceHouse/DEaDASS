package api.model.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Indicates an error that should produce an HTTP 400 Bad Request response.
 *
 * @author Max Meinhold <mxmeinhold@gmail.com>
 */
@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class BadRequestException extends DeadassException {

    public BadRequestException(String s) {
        super(s);
    }
}
