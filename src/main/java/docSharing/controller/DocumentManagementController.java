package docSharing.controller;

import docSharing.Entities.Document;
import docSharing.service.AuthenticationService;
import docSharing.service.DocumentService;
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
@RequestMapping("api/v1/doc")
public class DocumentManagementController {

    @Autowired
    private DocumentService documentService;
    @Autowired
    AuthenticationService authenticationService;

    private static final Logger LOGGER = LogManager.getLogger(DocumentManagementController.class);

    @PostMapping("/create")
    public void createDocument(@RequestParam String title,
                               @RequestParam String token, @RequestParam Long folderId) {
        try {
            Long id = authenticationService.isLoggedIn(token);
            documentService.createDocument(id, title, folderId);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not logged in");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocumentById(@PathVariable Long id, @RequestParam String token) {
        try {
            Long userId = authenticationService.isLoggedIn(token);
            return new ResponseEntity<>(documentService.getDocumentById(id, userId), HttpStatus.OK);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not logged in");
        }
    }

    @GetMapping("/all")
    public ResponseEntity<Set<Document>> getAllDocuments(@RequestParam String token) {
        try {
            Long userId = authenticationService.isLoggedIn(token);
            return ResponseEntity.ok(documentService.getAllDocuments(userId));
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not logged in");
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }

    @PostMapping("/import")
    public ResponseEntity<Void> importDocument(@RequestParam String title, @RequestParam String token,
                                               @RequestParam Long folderId, @RequestBody String body) {
        try {
            Long id = authenticationService.isLoggedIn(token);
            documentService.createDocument(id, title, body, folderId);
        } catch (IllegalStateException e) {
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
        } catch (IllegalStateException e) {
            LOGGER.debug(String.format("move document request failed - token:%s, document:%d to destination folder:%d - " + e.getMessage(), token, documentId, folderId));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not logged in");
        }
    }
}
