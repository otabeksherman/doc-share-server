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

    /**
     * moves a folder into another folder.
     * @param userId the id of the user requesting the move.
     * @param folderId the id of the folder to move.
     * @param destinationId the id of the folder where to move the document. if this is -1 will move the document one folder closer to root.
     * @throws IllegalArgumentException if the user id, folder id or destination id is incorrect,
     * and if the user is not an owner of the folder and the destination folder.
     */
    public void moveFolder(Long userId, Long folderId, Long destinationId) {
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            LOGGER.debug(String.format("user:%d doesn't exist", userId));
            throw new IllegalArgumentException(String.format("User with ID: '%d' doesn't exist", userId));
        }
        Optional<Folder> folder = folderRepository.findById(folderId);
        if (!folder.isPresent()) {
            LOGGER.debug(String.format("failing to get folder:%d - doesn't exist", folderId));
            throw new IllegalArgumentException(String.format("folder with ID: '%d' doesn't exist", folderId));
        }
        if (destinationId == -1) {
            if (folder.get().getParentFolder().getParentFolder() == null) {
                LOGGER.debug(String.format("failing to move folder:%d to parent - folder:%d is a root folder", folderId, destinationId));
                throw new IllegalArgumentException(String.format("folder:%d is in your root folder", folderId));
            }
            destinationId = folder.get().getParentFolder().getParentFolder().getId();
        }
        Optional<Folder> destinationFolder = folderRepository.findById(destinationId);
        if (!destinationFolder.isPresent()) {
            LOGGER.debug(String.format("failing to get folder:%d - doesn't exist", destinationId));
            throw new IllegalArgumentException(String.format("folder with ID: '%d' doesn't exist", destinationId));
        }

        if (folder.get().getOwner() != user.get() || destinationFolder.get().getOwner() != user.get()) {
            LOGGER.debug(String.format("move failed - user:%d is not owner of folder:%d and/or folder:%d", userId,folderId, destinationId));
            throw new IllegalArgumentException(
                    String.format("User with ID: '%d' is not owner of folder:%d or folder:%d", userId, folderId, destinationId));
        }

        folder.get().setParentFolder(destinationFolder.get());
        folderRepository.save(folder.get());
        LOGGER.debug(String.format("moved folder:%d to folder:%d", folderId, destinationId));
    }
}
