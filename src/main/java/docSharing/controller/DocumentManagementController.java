package docSharing.controller;

import docSharing.service.AuthenticationService;
import docSharing.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
                               @RequestParam String token, @RequestParam Long folderId) {
        try {
            Long id = authenticationService.isLoggedIn(token);
            documentService.createDocument(id, title, folderId);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not logged in");
        }
    }
}
