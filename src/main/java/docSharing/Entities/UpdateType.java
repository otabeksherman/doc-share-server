package docSharing.Entities;

public enum UpdateType {
    DELETE("Delete"),
    APPEND("Append"),
    DELETE_RANGE("Delete in range"),
    APPEND_RANGE("Append in range");

    private final String type;

    UpdateType(String type) {
        this.type = type;
    }
}
