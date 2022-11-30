package docSharing.controller;

import docSharing.Entities.Document;
import docSharing.service.AuthenticationService;
import docSharing.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Set;

@RestController
@CrossOrigin
@RequestMapping("api/v1/doc")
public class DocumentManagementController {

    @Autowired
    private DocumentService documentService;
    @Autowired
    AuthenticationService authenticationService;

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
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not logged in");
        }
    }

    /**
     * @param id - for document
     * @param token - for user
     * @return document with that id
     * @throws ResponseStatusException if the user not logged in.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocumentById(@PathVariable Long id, @RequestParam String token) {
        try {
            Long userId = authenticationService.isLoggedIn(token);
            return new ResponseEntity<>(documentService.getDocumentById(id, userId), HttpStatus.OK);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not logged in");
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
}
