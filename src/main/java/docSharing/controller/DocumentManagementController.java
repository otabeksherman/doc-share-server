package docSharing.controller;

import docSharing.Entities.*;
import docSharing.service.AuthenticationService;
import docSharing.service.ChangeLogService;
import docSharing.service.DocumentService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@CrossOrigin
@RequestMapping("api/v1/doc")
public class DocumentManagementController {

    @Autowired
    private DocumentService documentService;
    @Autowired
    AuthenticationService authenticationService;
    @Autowired
    ChangeLogService changeLogService;

    private static final Logger LOGGER = LogManager.getLogger(DocumentManagementController.class);

    /**
     * Create a new document using the data received from the client.
     * @param title - document's title
     * @param token - user's token
     * @param folderId - The folder's id to add the document to.
     * @throws ResponseStatusException if the user not logged in.
     */
    @PostMapping("/create")
    public void createDocument(@RequestParam String title,
                               @RequestParam String token, @RequestParam Long folderId) {
        try {
            Long id = authenticationService.isLoggedIn(token);
            documentService.createDocument(id, title, folderId);
            LOGGER.info("document created");
        } catch (IllegalArgumentException e) {
            LOGGER.debug(String.format("The user with token: %s not logged in!",token));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "create document failed - " + e.getMessage());
        }
    }

    /**
     * @param id - for document
     * @param token - for user
     * @return document with that id
     * @throws ResponseStatusException if the user not logged in.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DocumentAndRole> getDocumentById(@PathVariable Long id, @RequestParam String token) {
        try {
            User user = authenticationService.getUserByToken(token);
            Document doc = documentService.getDocumentById(id, user.getId());
            DocumentAndRole docAndRole = new DocumentAndRole(doc,doc.getUserRole(user));
            return new ResponseEntity<>(docAndRole, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            LOGGER.debug(String.format("The user with token: %s not logged in!",token));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not logged in");
        }
    }

    @PostMapping("/import")
    public ResponseEntity<Void> importDocument(@RequestParam String title, @RequestParam String token,
                                               @RequestParam Long folderId, @RequestBody String body) {
        try {
            Long id = authenticationService.isLoggedIn(token);
            documentService.createDocument(id, title, body, folderId);
            LOGGER.info("document created");
        } catch (IllegalArgumentException e) {
            LOGGER.debug(String.format("The user with token: %s not logged in!",token));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not logged in");
        }
        return ResponseEntity.noContent().build();
    }

    /**
     * moves a document into a folder. gets called from a REST patch call.
     * @param documentId the document to move.
     * @param token the token the user got after logging in.
     * @param folderId the destination folders id, or -1 to move the document one step closer to root.
     * @return a response entity with a code of 204 if the operation was successful.
     * @throws ResponseStatusException if the operation failed for any reason.
     */
    @PatchMapping("/move/{documentId}")
    public ResponseEntity<Void> moveDocument(@PathVariable Long documentId, @RequestParam String token, @RequestParam Long folderId) {
        LOGGER.info(String.format("move document request got - token:%s, document:%d to destination folder:%d", token, documentId, folderId));
        try {
            Long id = authenticationService.isLoggedIn(token);
            documentService.moveDocument(id, documentId, folderId);
            LOGGER.debug(String.format("move document request success - token:%s, document:%d to destination folder:%d", token, documentId, folderId));
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            LOGGER.debug(String.format("move document request failed - token:%s, document:%d to destination folder:%d - " + e.getMessage(), token, documentId, folderId));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not logged in");
        }
    }

    @GetMapping("allowedUsers")
    public ResponseEntity<Map<Role, List<UserResponse>>> getAllowedUsers(@RequestParam String token,
                                                          @RequestParam Long docId) {
        Map<Role, List<UserResponse>> res = new HashMap<>();
        try {
            Long userId = authenticationService.isLoggedIn(token);
            Document document = documentService.getDocumentById(docId, userId);

            UserResponse owner = new UserResponse(document.getOwner().getName(),
                    document.getOwner().getEmail());
            res.put(Role.OWNER, List.of(owner));
            List<UserResponse> viewers = getUsersAsUserResponse(document.getViewers());
            res.put(Role.VIEWER, viewers);
            List<UserResponse> editors = getUsersAsUserResponse(document.getEditors());
            res.put(Role.EDITOR, editors);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not logged in");
        }
        return new ResponseEntity<>(res, HttpStatus.OK);
    }

    @PatchMapping("share")
    public ResponseEntity<Void> shareDocument(@RequestBody ShareRequest request) {
        try {
            Long userId = authenticationService.isLoggedIn(request.getToken());
            if (!authenticationService.doesExistByEmail(request.getEmail())) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User doesn't exist");
            }
            documentService.shareDocument(userId, request.getEmail(),
                    request.getDocumentId(), request.getRole());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not logged in");
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("logs")
    public ResponseEntity<List<ChangeLog>> getChangeLogs(@RequestParam String token,
                                                   @RequestParam Long docId) {
        try {
            Long userId = authenticationService.isLoggedIn(token);
            return new ResponseEntity<>(changeLogService.getChangeLogs(docId), HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not logged in");
        }
    }

    private List<UserResponse> getUsersAsUserResponse(Set<User> users) {
        return users.stream()
                .map(user -> new UserResponse(user.getName(), user.getEmail()))
                .collect(Collectors.toList());
    }
}
