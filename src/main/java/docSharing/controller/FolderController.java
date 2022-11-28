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
            LOGGER.debug(String.format("crete folder request success - token:%s, new folder name:%s, parent folder:%d", token, name, parentId));
            return ResponseEntity.accepted().build();
        } catch (IllegalArgumentException e) {
            LOGGER.debug(String.format("crete folder request failed - token:%s, new folder name:%s, parent folder:%d - " + e.getMessage(), token, name, parentId));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
