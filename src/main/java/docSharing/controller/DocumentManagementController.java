package docSharing.controller;

import docSharing.Entities.Document;
import docSharing.service.AuthenticationService;
import docSharing.service.DocumentService;
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

    @PostMapping("/create")
    public void createDocument(@RequestParam String title,
                               @RequestParam String token) {
        try {
            Long id = authenticationService.isLoggedIn(token);
            documentService.createDocument(id, title);
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
}
