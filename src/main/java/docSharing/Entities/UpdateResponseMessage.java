package docSharing.Entities;

public class UpdateResponseMessage extends Message {
    private String email;

    public UpdateResponseMessage(String content, long documentId, UpdateType type, int position, String email) {
        super(content, documentId, type, position);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
