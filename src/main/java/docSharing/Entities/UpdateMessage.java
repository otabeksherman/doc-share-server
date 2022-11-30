package docSharing.Entities;

public class UpdateMessage {
    private String user;
    private String content;
    private Long documentId;
    private UpdateType type;
    private int position;

    public UpdateMessage() {
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getContent() {
        return content;
    }

    public Long getDocumentId() {
        return documentId;
    }


    public void setContent(String content) {
        this.content = content;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }
    public UpdateType getType() {
        return type;
    }
    public void setType(UpdateType type) {
        this.type = type;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return "UpdateMessage{" +
                "userId=" + user +
                ", content='" + content + '\'' +
                ", documentId=" + documentId +
                '}';
    }
}

