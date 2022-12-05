package docSharing.Entities;

public class UpdateMessage extends Message {
    private String user;

    public UpdateMessage() {}

    public UpdateMessage(String user, String content, long documentId, UpdateType type, int position) {
        super(content, documentId, type, position);
        this.user = user;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}

