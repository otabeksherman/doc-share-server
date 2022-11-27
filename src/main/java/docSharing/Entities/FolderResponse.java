package docSharing.Entities;

import java.util.Set;

public class FolderResponse {
    Folder folder;

    Set<Folder> subFolders;

    public FolderResponse(Folder folder, Set<Folder> subFolders) {
        this.folder = folder;
        this.subFolders = subFolders;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    public Set<Folder> getSubFolders() {
        return subFolders;
    }

    public void setSubFolders(Set<Folder> subFolders) {
        this.subFolders = subFolders;
    }
}
