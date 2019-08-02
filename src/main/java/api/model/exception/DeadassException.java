package api.model.exception;

/**
 * A superclass for categorising DEaDASS's exceptions.
 *
 * @author Max Meinhold <mxmeinhold@gmail.com>
 */
public abstract class DeadassException extends Exception {

    /**
     * Constructs a DeadassException with a null message.
     */
    public DeadassException() {
        super();
    }


    /**
     * Consturcts a DeadassException with a specific message.
     *
     * @param message the message to use
     */
    public DeadassException(String message) {
        super(message);
    }


    /**
     * Constructs a new DeadassException and associates a causing Exception
     *
     * @param cause the cause to associate with this exception.
     */
    public DeadassException(Throwable cause) {
        super(cause);
    }


    /**
     * Constructs a message with a causing exception and a specific message
     *
     * @param message the message to use
     * @param cause   the cause to associate with this exception
     */
    public DeadassException(String message, Throwable cause) {
        super(message, cause);
    }
}
