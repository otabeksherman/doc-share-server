package docSharing.controller;

import docSharing.Entities.Document;
import docSharing.Entities.Folder;
import docSharing.Entities.User;
import docSharing.service.AuthenticationService;
import docSharing.service.DocumentService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class DocumentManagementControllerUnitTests {

    @Autowired
    DocumentManagementController documentManagementController;

    @MockBean
    AuthenticationService authenticationService;

    @MockBean
    DocumentService documentService;

    User user;
    Folder mainFolder;

    Document document;

    @BeforeEach
    void setup() {
        user = new User();
        user.setEmail("gideon.jaffe@gmail.com");
        user.setPassword("qwer1234");
        user.setId(5L);
        user.setActivated(true);

        mainFolder = new Folder(user);
        mainFolder.setId(1L);

        document = new Document(user, "title", mainFolder);
    }

    @Test
    void createDocument_TokenInvalid_throwsResponseStatusException() {
        when(authenticationService.isLoggedIn("qweASD123zxc")).thenThrow(IllegalArgumentException.class);

        assertThrows(ResponseStatusException.class, () -> documentManagementController.createDocument("this fails", "qweASD123zxc", mainFolder.getId()));
    }

    @Test
    void createDocument_FolderDoesNotExist_throwsResponseStatusException() {
        when(authenticationService.isLoggedIn("qweASD123zxc")).thenReturn(user.getId());
        doThrow(IllegalArgumentException.class).when(documentService).createDocument(user.getId(), "this fails", -4L);

        assertThrows(ResponseStatusException.class, () -> documentManagementController.createDocument("this fails", "qweASD123zxc", -4L));
    }

    @Test
    void createDocument_GoodRequest_doesNotThrow() {
        assertDoesNotThrow(() -> documentManagementController.createDocument("this fails", "qweASD123zxc", mainFolder.getId()));
    }

    @Test
    void getDocumentById_TokenInvalid_throwsResponseStatusException() {
        when(authenticationService.isLoggedIn("qweASD123zxc")).thenThrow(IllegalArgumentException.class);

        assertThrows(ResponseStatusException.class, () -> documentManagementController.getDocumentById(document.getId(), "qweASD123zxc"));
    }

    @Test
    void getDocumentById_ServiceThrows_throwsResponseStatusException () {
        when(authenticationService.isLoggedIn("qweASD123zxc")).thenReturn(user.getId());
        when(documentService.getDocumentById(document.getId(), user.getId())).thenThrow(IllegalArgumentException.class);

        assertThrows(ResponseStatusException.class, () -> documentManagementController.getDocumentById(document.getId(), "qweASD123zxc"));
    }

    @Test
    void getDocumentById_GoodRequest_returnsOkResponseWithDocument () {
        when(authenticationService.isLoggedIn("qweASD123zxc")).thenReturn(user.getId());
        when(documentService.getDocumentById(document.getId(), user.getId())).thenReturn(document);

        ResponseEntity<Document> response = documentManagementController.getDocumentById(document.getId(), "qweASD123zxc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(document, response.getBody());
    }

    @Test
    void importDocument_TokenInvalid_throwsResponseStatusException() {
        when(authenticationService.isLoggedIn("qweASD123zxc")).thenThrow(IllegalArgumentException.class);

        assertThrows(ResponseStatusException.class, () -> documentManagementController.importDocument("title", "qweASD123zxc", mainFolder.getId(), "this is body"));
    }

    @Test
    void importDocument_ServiceError_throwsResponseStatusException() {
        when(authenticationService.isLoggedIn("qweASD123zxc")).thenReturn(user.getId());
        doThrow(IllegalArgumentException.class).when(documentService).createDocument(user.getId(), "title","this is body", mainFolder.getId());

        assertThrows(ResponseStatusException.class, () -> documentManagementController.importDocument("title", "qweASD123zxc", mainFolder.getId(), "this is body"));
    }

    @Test
    void importDocument_GoodRequest_returnNoContentResponse() {
        when(authenticationService.isLoggedIn("qweASD123zxc")).thenReturn(user.getId());
        ResponseEntity<Void> response = documentManagementController.importDocument("title", "qweASD123zxc", mainFolder.getId(), "this is body");

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void moveDocument_InvalidToken_throwsResponseStatusException() {
        when(authenticationService.isLoggedIn("qweASD123zxc")).thenThrow(IllegalArgumentException.class);

        assertThrows(ResponseStatusException.class, () -> documentManagementController.moveDocument(document.getId(), "qweASD123zxc", mainFolder.getId()));
    }

    @Test
    void moveDocument_ServiceError_throwsResponseStatusException() {
        when(authenticationService.isLoggedIn("qweASD123zxc")).thenReturn(user.getId());
        doThrow(IllegalArgumentException.class).when(documentService).moveDocument(user.getId(), document.getId(), mainFolder.getId());

        assertThrows(ResponseStatusException.class, () -> documentManagementController.moveDocument(document.getId(), "qweASD123zxc", mainFolder.getId()));
    }

    @Test
    void moveDocument_GoodRequest_returnsNoContentResponse() {
        when(authenticationService.isLoggedIn("qweASD123zxc")).thenReturn(user.getId());

        ResponseEntity<Void> response = documentManagementController.moveDocument(document.getId(), "qweASD123zxc", mainFolder.getId());

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }
}
