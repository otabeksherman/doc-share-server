package docSharing.service;

import docSharing.Entities.Folder;
import docSharing.Entities.User;
import docSharing.repository.FolderRepository;
import docSharing.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private static final Logger LOGGER = LogManager.getLogger(FolderService.class);

    /**
     * Gets the user main folder.
     * If the user doesn't have a main folder it will create a main folder for him.
     * @param userId the user's id.
     * @return the user's main folder.
     * @throws IllegalArgumentException if the user doesn't exist.
     */
    public Folder getMainFolder(Long userId) {
        Optional<Folder> mainFolder = folderRepository.findByOwnerIdAndParentFolderIsNull(userId);
        if (mainFolder.isPresent()) {
            return mainFolder.get();
        }

        Optional<User> byId = userRepository.findById(userId);
        if (!byId.isPresent()) {
            LOGGER.debug(String.format("user:%d doesn't exist", userId));
            throw new IllegalArgumentException(String.format("user:%d doesn't exist", userId));
        }

        LOGGER.info(String.format("Main folder for user:%d doesn't exist, creating new folder", userId));
        Folder folder = new Folder(byId.get());
        folderRepository.save(folder);
        return folder;
    }

    /**
     * Gets a folder by id.
     * @param userId the user's id.
     * @param folderId the wanted folder.
     * @return the folder got.
     * @throws IllegalArgumentException if the folder doesn't exist or the folder is not owned by the user.
     */
    public Folder getFolder(Long userId, Long folderId) {
        Optional<Folder> optFolder = folderRepository.findById(folderId);
        if (!optFolder.isPresent()) {
            LOGGER.debug(String.format("failing to get folder:%d - folder doesn't exist", folderId));
            throw new IllegalArgumentException("Folder doesn't exist");
        }
        Folder folder = optFolder.get();
        if (!Objects.equals(folder.getOwner().getId(), userId)) {
            LOGGER.debug(String.format("failing to get folder:%d - user:%d is not owner", folderId, userId));
            throw new IllegalArgumentException("Folder not owned by user");
        }
        return folder;
    }

    /**
     * Gets the sub folders of a certain folder that the user owns.
     * @param userId the user's id.
     * @param folderId the parent folder.
     * @return the sub folders of a parent folder. returns only folders the user owns.
     */
    public Set<Folder> getSubFolders(Long userId, Long folderId) {
        LOGGER.debug(String.format("returning sub folders of folder:%d - for user:%d", folderId, userId));
        return folderRepository.findByParentFolderIdAndOwnerId(folderId, userId);
    }

    /**
     * creates a folder in a certain parent folder.
     * @param userId the user's id.
     * @param name a name for the newly created folder.
     * @param parentId the id of the parent folder.
     * @throws IllegalArgumentException if the user id doesn't exist, or if the parent folder doesn't exist.
     */
    public void createFolder(Long userId, String name, Long parentId) {
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            LOGGER.debug(String.format("cant create folder for user:%d - user doesn't exist", userId));
            throw new IllegalArgumentException(String.format("User of id %d doesn't exist", userId));
        }
        Optional<Folder> folder = folderRepository.findById(parentId);
        if (!folder.isPresent()) {
            LOGGER.debug(String.format("cant create folder for user:%d in folder:%d - folder doesn't exist", userId, parentId));
            throw new IllegalArgumentException(String.format("Folder of id %d doesn't exist", parentId));
        }
        LOGGER.info(String.format("creating folder \"%s\" for user:%d in folder:%d ", name, userId, parentId));
        Folder newFolder = new Folder(user.get());
        newFolder.setName(name);
        newFolder.setParentFolder(folder.get());
        folderRepository.save(newFolder);

    }
}
