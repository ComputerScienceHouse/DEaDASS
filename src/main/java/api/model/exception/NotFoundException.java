package api.model.exception;

/**
 * An exception to be thrown when a resource is not found.
 * Indicates that input was invalid or an http 404 response should be sent.
 * @author Max Meinhold <mxmeinhold@gmail.com>
 */
public class NotFoundException extends DeadassException{
    public NotFoundException(String message) {
        super(message);
    }
}
