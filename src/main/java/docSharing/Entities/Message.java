package docSharing.Entities;

public class Message {
    private String content;
    private Long documentId;
    private UpdateType type;
    private int position;

    public Message() {}

    public Message(String content, long documentId, UpdateType type, int position) {
        this.content = content;
        this.documentId = documentId;
        this.type = type;
        this.position = position;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getDocumentId() {
        return documentId;
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
}
