package api.model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;

import java.util.HashMap;
import java.util.Map;

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

        public String toString() {
            return this.type;
        }
    }

    public Message(String message, Type type) {
        this.message = message;
        this.type = type;
    }

    public String asJSON() {
        return "{\"message\":\"" + this.message + "\", \"type\":\"" + this.type + "\"}";
    }
}
