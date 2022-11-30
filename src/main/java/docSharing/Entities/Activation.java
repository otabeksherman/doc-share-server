package docSharing.Entities;

public class Activation {
    private String userEmail;
    private String token;

    public Activation() {
    }

    public Activation(String userEmail, String token) {
        this.userEmail = userEmail;
        this.token = token;
    }

    public String getUserEmail() {
        return this.userEmail;
    }

    public String getToken() {
        return this.token;
    }
}
