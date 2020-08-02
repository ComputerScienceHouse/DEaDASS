package api.model.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * An exception to be thrown when a resource is not found.
 * Indicates that input was invalid or an http 404 response should be sent.
 *
 * @author Max Meinhold <mxmeinhold@gmail.com>
 */
@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NotFoundException extends DeadassException {

    public NotFoundException(String message) {
        super(message);
    }
}
