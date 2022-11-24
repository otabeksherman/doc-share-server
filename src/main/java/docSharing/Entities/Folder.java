package docSharing.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "folders")
public class Folder {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JsonIgnore
    private User owner;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.REMOVE, CascadeType.PERSIST, CascadeType.MERGE})
    @JoinColumn(name="parent_id")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Folder parentFolder;

    @OneToMany(mappedBy = "parentFolder", fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
    @JsonIgnore
    private Set<Folder> innerFolders;

    @OneToMany(mappedBy = "folder", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Set<Document> innerDocuments;

    public Folder() {}

    public Folder(User owner) {
        this.owner = owner;
        this.name = "Home";
        this.innerDocuments = new HashSet<>();
        this.innerFolders = new HashSet<>();
        this.parentFolder = null;
    }

    public void addDocument(Document doc) {
        innerDocuments.add(doc);
        doc.setFolder(this);
    }

    public void removeDocument(Document doc) {
        innerDocuments.remove(doc);
        doc.setFolder(null);
    }

    public Set<Document> removeAllDocuments() {
        for (Document doc :
                innerDocuments) {
            doc.setFolder(null);
        }
        Set<Document> docs = innerDocuments;
        innerDocuments.clear();
        return docs;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Folder getParentFolder() {
        return parentFolder;
    }

    public Set<Document> getInnerDocuments() {
        return innerDocuments;
    }

    public Set<Folder> getInnerFolders() {
        return innerFolders;
    }

    public User getOwner() {
        return owner;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setParentFolder(Folder parentFolder) {
        this.parentFolder = parentFolder;
    }
}
