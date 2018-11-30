package api.model;

public class CreateResponse {
    
    private String message;
    private String password;

    public CreateResponse(String msg, String pass) {
        this.message = msg;
        this.password = pass;
    }

}
