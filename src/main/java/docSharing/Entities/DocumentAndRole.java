package docSharing.Entities;

public class DocumentAndRole {
    Document document;
    Role role;

    public DocumentAndRole(Document document, Role role) {
        this.document = document;
        this.role = role;
    }

    public Document getDocument() {
        return document;
    }

    public Role getRole() {
        return role;
    }

    public void setDocument(Document document) {
        this.document = document;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
