package api.model;

/**
 * An object for collecting a message and a status code
 * @deprecated
 * @author Max Meinhold <mxmeinhold@gmail.com>
 */
public class Message implements JSONUtils.JSONable {

    private String message;
    private Type type;


    /**
     * A status code representation
     * @deprecated
     */
    public enum Type {
        SUCCESS("success"),
        ERROR("error"),
        MESSAGE("message");

        private final String type;

        Type(String type) {
            this.type = type;
        }

        public String toString() {
            return this.type;
        }
    }


    /**
     * Creates a new message
     * @deprecated
     * @param message the message to transmit
     * @param type the status the message represents
     */
    public Message(String message, Type type) {
        this.message = message;
        this.type = type;
    }


    /**
     * Converts this message to a JSON string
     * @deprecated
     * @return a JSON string representing this message
     */
    public String asJSON() {
        return "{\"message\":\"" + this.message + "\", \"type\":\"" + this.type + "\"}";
    }
}
