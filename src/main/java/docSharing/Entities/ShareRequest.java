package docSharing.Entities;

public class ShareRequest {

    private String token;
    private String email;
    private Long documentId;
    private Role role;

    public String getToken() {
        return token;
    }

    public String getEmail() {
        return email;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public Role getRole() {
        return role;
    }
}
