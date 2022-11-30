package docSharing.controller;

import docSharing.Entities.Folder;
import docSharing.Entities.FolderResponse;
import docSharing.service.AuthenticationService;
import docSharing.service.FolderService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;
import java.util.Set;

@RestController
@CrossOrigin
@RequestMapping("api/v1/folder")
public class FolderController {
    @Autowired
    private FolderService folderService;

    @Autowired
    private AuthenticationService authenticationService;

    private static final Logger LOGGER = LogManager.getLogger(FolderService.class);

    /**
     * gets the main folder for the user. gets called from a REST Get call.
     * @param token the token the user got after logging in.
     * @return a response entity containing the folder with sub-folders and sub-documents.
     * @throws ResponseStatusException if the user is not logged in, or if the user doesn't exist.
     */
    @GetMapping
    public ResponseEntity<FolderResponse> getMainFolder(@RequestParam String token) {
        LOGGER.info(String.format("main folder request got - token:%s", token));
        try {
            Long id = authenticationService.isLoggedIn(token);
            Folder folder = folderService.getMainFolder(id);
            Set<Folder> subFolders = folderService.getSubFolders(id, folder.getId());
            LOGGER.debug(String.format("main folder request success for token:%s", token));
            return ResponseEntity.ok(new FolderResponse(folder, subFolders));
        } catch (IllegalArgumentException e) {
            LOGGER.debug(String.format("main folder request failed - " + e.getMessage(), token));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * gets a folder. gets called from a REST get call.
     * @param token the token the user got after logging in.
     * @param folderId the folder the user wants.
     * @return a response entity containing the folder with sub-folders and sub-documents.
     * @throws ResponseStatusException if the user is not logged in, and if the user doesn't own the folder.
     */
    @GetMapping("/{folderId}")
    public ResponseEntity<FolderResponse> getFolder(@RequestParam String token, @PathVariable Long folderId) {
        LOGGER.info(String.format("get folder:%d request got - token:%s", folderId, token));
        try {
            Long id = authenticationService.isLoggedIn(token);
            Folder folder = folderService.getFolder(id, folderId);
            Set<Folder> subFolders = folderService.getSubFolders(id, folder.getId());
            LOGGER.debug(String.format("get folder:%d request success for token:%s", folderId, token));
            return ResponseEntity.ok(new FolderResponse(folder, subFolders));
        } catch (IllegalArgumentException e) {
            LOGGER.debug(String.format("get folder:%d request failed for token:%s - " + e.getMessage(), folderId, token));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * gets a folder. gets called from a REST get call.
     * @param token the token the user got after logging in.
     * @param name a name for the newly created folder.
     * @param parentId the parent folder to create the new folder inside it.
     * @return a response entity.
     * @throws ResponseStatusException if the user is not logged in, and if the user doesn't own the parent folder.
     */
    @PostMapping("/{parentId}/new")
    public ResponseEntity<Void> createFolder(@RequestParam String token, @RequestParam String name, @PathVariable Long parentId) {
        LOGGER.info(String.format("create folder request got - token:%s, new folder name:%s, parent folder:%d", token, name, parentId));
        try {
            Long id = authenticationService.isLoggedIn(token);
            folderService.createFolder(id, name, parentId);
            LOGGER.debug(String.format("create folder request success - token:%s, new folder name:%s, parent folder:%d", token, name, parentId));
            return ResponseEntity.accepted().build();
        } catch (IllegalArgumentException e) {
            LOGGER.debug(String.format("create folder request failed - token:%s, new folder name:%s, parent folder:%d - " + e.getMessage(), token, name, parentId));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    /**
     * moves a folder into a sub folder. gets called from a REST patch call.
     * @param folderId the folder to move.
     * @param token the token the user got after logging in.
     * @param destinationId the destination folders id, or -1 to move the folder one step closer to root.
     * @return a response entity with a code of 204 if the operation was successful.
     * @throws ResponseStatusException if the operation failed for any reason.
     */
    @PatchMapping("/move/{folderId}")
    public ResponseEntity<Void> moveFolder(@PathVariable Long folderId, @RequestParam String token, @RequestParam Long destinationId) {
        LOGGER.info(String.format("move folder request got - token:%s, folder:%d to destination folder:%d", token, folderId, destinationId));
        if (Objects.equals(folderId, destinationId)) {
            LOGGER.debug(String.format("move folder request failed - token:%s, folder:%d to itself", token, folderId));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cant move a folder to itself");
        }
        try {
            Long id = authenticationService.isLoggedIn(token);
            folderService.moveFolder(id, folderId, destinationId);
            LOGGER.debug(String.format("move folder request success - token:%s, folder:%d to destination folder:%d", token, folderId, destinationId));
            return ResponseEntity.accepted().build();
        } catch (IllegalArgumentException e) {
            LOGGER.debug(String.format("move folder request failed - token:%s, folder:%d to destination folder:%d - " + e.getMessage(), token, folderId, destinationId));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
