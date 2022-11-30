package docSharing.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String title;

    @OneToOne(targetEntity = User.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Column(columnDefinition = "TEXT")
    private String body;

    @ManyToMany(fetch = FetchType.EAGER,
        cascade = {
            CascadeType.PERSIST,
            CascadeType.MERGE
        })
    @JoinTable(name = "document_viewers",
        joinColumns = { @JoinColumn(name = "document_id") },
        inverseJoinColumns = { @JoinColumn(name = "user_id") })
    @JsonIgnore
    private Set<User> viewers = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER,
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE
            })
    @JoinTable(name = "document_editors",
        joinColumns = { @JoinColumn(name = "document_id") },
        inverseJoinColumns = { @JoinColumn(name = "user_id") })
    @JsonIgnore
    private Set<User> editors = new HashSet<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnore
    private Folder folder;

    public Document() {}

    public Document(User user, String title, Folder folder) {
        this.title = title;
        this.owner = user;
        this.viewers.add(user);
        this.editors.add(user);
        this.folder = folder;
    }

    @JsonIgnore
    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    public void addViewer(User user) {
        this.viewers.add(user);
        user.addViewDocument(this);
    }

    public Set<User> getViewers() {
        return viewers;
    }

    public Set<User> getEditors() {
        return editors;
    }

    public void addEditor(User user) {
        this.editors.add(user);
        user.addEditDocument(this);
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    @JsonIgnore
    public User getOwner() {return owner;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Document document = (Document) o;
        return Objects.equals(id, document.id) && Objects.equals(title, document.title) && Objects.equals(owner, document.owner) && Objects.equals(body, document.body) && Objects.equals(folder, document.folder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, owner, body, folder);
    }
}
