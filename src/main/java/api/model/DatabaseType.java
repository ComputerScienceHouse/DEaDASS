package api.model;

public enum DatabaseType {

    MONGO("mongodb"), POSTGRES("postgresql"), MYSQL("mysql");

    public final String type;

    DatabaseType(String type) {
        this.type = type;
    }

    public String toString() {
        return this.type;
    }


    public static DatabaseType fromString(String type) {
        for (DatabaseType t : values()) {
            if (t.type.equalsIgnoreCase(type)) {
                return t;
            }
        }
        return null;
    }
}
