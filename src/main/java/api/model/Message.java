package api.model;

public class Message {
    private String message;
    private Type type;

    public enum Type {
        SUCCESS("success"),
        ERROR("error"),
        MESSAGE("message");

        private final String type;

        Type(String type) {
            this.type = type;
        }
    }

    public Message(String message, Type type) {
        this.message = message;
        this.type = type;
    }
}
