package api.model.exception;

/**
 * Indicates an error that should produce an HTTP 401 Bad Request response.
 * @author Max Meinhold <mxmeinhold@gmail.com>
 */
public class BadRequestException extends DeadassException {

    public BadRequestException(String s) {
        super(s);
    }
}
