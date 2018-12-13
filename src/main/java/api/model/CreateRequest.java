package api.model;

public class CreateRequest {

    private int poolID;
    private String name;
    private String purpose;
    private int type; // TODO enum?

    public int getPoolID() {
        return poolID;
    }

    public String getName() {
        return name;
    }

    public String getPurpose() {
        return purpose;
    }

    public int getType() {
        return type;
    }

}
