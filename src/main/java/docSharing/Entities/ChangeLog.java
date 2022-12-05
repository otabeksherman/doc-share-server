package docSharing.Entities;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity for keeping
 */
@Entity
@Table(name = "change_log")
public class ChangeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long documentId;

    private int startPosition;
    private int endPosition;
    private String email;

    private String body;
    private LocalDateTime lastModified;

    public ChangeLog(){}

    public ChangeLog(Long documentId, int position, String email, String body) {
        this.documentId = documentId;
        this.startPosition = position;
        this.endPosition = startPosition + 1;
        this.email = email;
        this.body = body;
        this.lastModified = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public int getStartPosition() {
        return startPosition;
    }

    public void setStartPosition(int startPosition) {
        this.startPosition = startPosition;
    }

    public String getEmail() {
        return email;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void appendText(String txt) {
        this.body = body + txt;
    }

    public int getEndPosition() {
        return endPosition;
    }

    public void setEndPosition(int endPosition) {
        this.endPosition = endPosition;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public void forwardChangeLogStartIndex() {
        this.startPosition = startPosition + 1;
    }

    public void forwardChangeLogEndIndex() {
        this.endPosition = endPosition + 1;
    }

    public void forwardChangeLogIndexes() {
        forwardChangeLogStartIndex();
        forwardChangeLogEndIndex();
    }
}
