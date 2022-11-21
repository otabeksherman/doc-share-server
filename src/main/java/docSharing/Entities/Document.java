package docSharing.Entities;

import javax.persistence.*;
import java.util.HashSet;
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
    private Set<User> viewers = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER,
            cascade = {
                    CascadeType.PERSIST,
                    CascadeType.MERGE
            })
    @JoinTable(name = "document_editors",
        joinColumns = { @JoinColumn(name = "document_id") },
        inverseJoinColumns = { @JoinColumn(name = "user_id") })
    private Set<User> editors = new HashSet<>();

    public Document() {}

    public Document(User user, String title) {
        this.title = title;
        this.owner = user;
        this.viewers.add(user);
        this.editors.add(user);
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
}
