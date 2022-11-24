package docSharing.controller;

import docSharing.Entities.Folder;
import docSharing.Entities.FolderResponse;
import docSharing.service.AuthenticationService;
import docSharing.service.FolderService;
import docSharing.service.UserService;
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

    @GetMapping
    public ResponseEntity<FolderResponse> getMainFolder(@RequestParam String token) {
        try {
            Long id = authenticationService.isLoggedIn(token);
            Folder folder = folderService.getMainFolder(id);
            Set<Folder> subFolders = folderService.getSubFolders(id, folder.getId());
            return new ResponseEntity<>(new FolderResponse(folder, subFolders), HttpStatus.OK);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not logged in");
        }
    }

    @GetMapping("/{folderId}")
    public ResponseEntity<FolderResponse> getFolder(@RequestParam String token, @PathVariable Long folderId) {
        try {
            Long id = authenticationService.isLoggedIn(token);
            Folder folder = folderService.getFolder(id, folderId);
            Set<Folder> subFolders = folderService.getSubFolders(id, folder.getId());
            return new ResponseEntity<>(new FolderResponse(folder, subFolders), HttpStatus.OK);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not logged in");
        }
    }

    @PostMapping("/{parentId}/new")
    public ResponseEntity<Void> createFolder(@RequestParam String token, @RequestParam String name, @PathVariable Long parentId) {
        try {
            Long id = authenticationService.isLoggedIn(token);
            folderService.createFolder(id, name, parentId);
            return ResponseEntity.accepted().build();
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not logged in");
        }
    }
}
