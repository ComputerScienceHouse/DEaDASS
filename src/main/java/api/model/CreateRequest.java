package api.model;

public class CreateRequest {

    private String uid;
    private String name;
    private String purpose;
    private String type; // TODO enum?

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public String getPurpose() {
        return purpose;
    }

    public String getType() {
        return type;
    }

}
