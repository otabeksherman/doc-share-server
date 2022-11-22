package docSharing.service;

import docSharing.Entities.Folder;
import docSharing.Entities.User;
import docSharing.repository.FolderRepository;
import docSharing.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
public class FolderService {

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private UserRepository userRepository;

    public Folder getMainFolder(Long userId) {
        Optional<Folder> mainFolder = folderRepository.findByOwnerIdAndParentFolderIsNull(userId);
        if (!mainFolder.isPresent()) {
            Optional<User> byId = userRepository.findById(userId);
            Folder folder = new Folder(byId.get());
            folderRepository.save(folder);
            return folder;
        }
        return mainFolder.get();
    }

    public Folder getFolder(Long userId, Long folderId) {
        Optional<Folder> optFolder = folderRepository.findById(folderId);
        if (!optFolder.isPresent()) {
            throw new IllegalArgumentException("Folder doesn't exist");
        }
        Folder folder = optFolder.get();
        if (!Objects.equals(folder.getOwner().getId(), userId)) {
            throw new IllegalArgumentException("Folder not owned by user");
        }
        return folder;
    }

    public Set<Folder> getSubFolders(Long userId, Long folderId) {
        Set<Folder> folders = folderRepository.findByParentFolderIdAndOwnerId(folderId, userId);
        return folders;
    }

    public void createFolder(Long userId, String name, Long parentId) {
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            throw new IllegalArgumentException(String.format("User of id %d doesn't exist", userId));
        }
        Optional<Folder> folder = folderRepository.findById(parentId);
        if (!folder.isPresent()) {
            throw new IllegalArgumentException(String.format("Folder of id %d doesn't exist", parentId));
        }
        Folder newFolder = new Folder(user.get());
        newFolder.setName(name);
        newFolder.setParentFolder(folder.get());
        folderRepository.save(newFolder);
    }
}
