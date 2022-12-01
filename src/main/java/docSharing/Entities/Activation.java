package docSharing.Entities;

public class Activation {
    private String email;
    private String token;

    public Activation() {
    }

    public Activation(String userEmail, String token) {
        this.email = userEmail;
        this.token = token;
    }

    public String getEmail() {
        return this.email;
    }

    public String getToken() {
        return this.token;
    }
}
