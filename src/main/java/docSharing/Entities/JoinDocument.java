package docSharing.Entities;

public class JoinDocument {
    private String user;
    private Long docId;
    public JoinDocument() {
    }
    public void setDocId(Long docId) {
        this.docId = docId;
    }

    public Long getDocId() {
        return docId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
