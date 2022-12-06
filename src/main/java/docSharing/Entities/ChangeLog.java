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
    private UpdateType updateType;

    public ChangeLog(){}

    public ChangeLog(Long documentId, int position, String email, String body, UpdateType updateType) {
        this.documentId = documentId;
        this.email = email;
        this.body = body;
        this.lastModified = LocalDateTime.now();
        this.updateType = updateType;
        initPositions(updateType, position, body);
    }

    private void initPositions(UpdateType updateType, int position, String body) {
        switch (updateType) {
            case APPEND:
                 this.startPosition = position;
                 this.endPosition = position + 1;
                 break;
            case APPEND_RANGE:
                this.startPosition = position;
                this.endPosition = position + body.length();
                break;
            case DELETE:
                this.endPosition = position;
                this.startPosition = position - 1;
                break;
            case DELETE_RANGE:
                this.endPosition = position;
                this.startPosition = position - body.length();
                break;
            default:
                throw new UnsupportedOperationException();
        }
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

    public void appendTextToHead(String txt) {
        this.body = txt + body;
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

    public UpdateType getUpdateType() {
        return updateType;
    }

    public void forwardChangeLogStartIndex() {
        forwardChangeLogStartIndex(1);
    }

    public void forwardChangeLogStartIndex(int steps) {
        this.startPosition = startPosition + steps;
    }

    public void forwardChangeLogEndIndex() {
        forwardChangeLogEndIndex(1);
    }

    public void forwardChangeLogEndIndex(int steps) {
        this.endPosition = endPosition + steps;
    }

    public void forwardChangeLogIndexes() {
        forwardChangeLogStartIndex();
        forwardChangeLogEndIndex();
    }

    public void forwardChangeLogIndexes(int steps) {
        forwardChangeLogStartIndex(steps);
        forwardChangeLogEndIndex(steps);
    }

    public void backChangeLogStartIndex() {
        backChangeLogStartIndex(1);
    }

    public void backChangeLogStartIndex(int steps) {
        this.endPosition = endPosition - steps;
    }

    public void backChangeLogEndIndex() {
        backChangeLogEndIndex(1);
    }

    public void backChangeLogEndIndex(int steps) {
        this.endPosition = endPosition - steps;
    }

    public void backChangeLogIndexes() {
        forwardChangeLogStartIndex(1);
        forwardChangeLogEndIndex(1);
    }

    public void backChangeLogIndexes(int steps) {
        forwardChangeLogStartIndex(steps);
        forwardChangeLogEndIndex(steps);
    }
}
