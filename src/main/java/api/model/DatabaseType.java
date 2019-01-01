package api.model;

public enum DatabaseType {

    MONGO("mongodb"), POSTGRES("postgresql"), MYSQL("mysql");

    private final String type;

    DatabaseType(String type) {
        this.type = type;
    }

    public String toString() {
        return this.type;
    }
}
