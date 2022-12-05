package docSharing.Entities;

import java.util.Objects;

public class LoginResponse {
    private String token;
    private String email;

    public LoginResponse(String token, String email) {
        this.token = token;
        this.email = email;
    }

    public String getToken() {
        return token;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LoginResponse response = (LoginResponse) o;
        return Objects.equals(token, response.token) && Objects.equals(email, response.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(token, email);
    }
}
