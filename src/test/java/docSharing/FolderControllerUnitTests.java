package docSharing;

import docSharing.Entities.Folder;
import docSharing.Entities.FolderResponse;
import docSharing.Entities.User;
import docSharing.controller.FolderController;
import docSharing.service.AuthenticationService;
import docSharing.service.FolderService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class FolderControllerUnitTests {

    @Autowired
    FolderController folderController;

    @MockBean
    FolderService folderService;

    @MockBean
    AuthenticationService authenticationService;

    static User user;
    static Folder mainFolder;

    @BeforeAll
    static void setup() {
        user = new User();
        user.setEmail("gideon.jaffe@gmail.com");
        user.setPassword("qwer1234");
        user.setId(5L);
        user.setActivated(true);

        mainFolder = new Folder(user);
    }

    @Test
    void getMainFolder_UserNotLoggedIn_throwsResponseStatusException() {
        when(authenticationService.isLoggedIn("qweASD123zxc")).thenThrow(IllegalArgumentException.class);

        assertThrows(ResponseStatusException.class,() -> folderController.getMainFolder("qweASD123zxc"));
    }

    @Test
    void getMainFolder_GoodRequest_returnsMainFolder() {
        when(authenticationService.isLoggedIn("qweASD123zxc")).thenReturn(user.getId());
        when(folderService.getMainFolder(user.getId())).thenReturn(mainFolder);
        when(folderService.getSubFolders(user.getId(), mainFolder.getId())).thenReturn(Set.of());

        ResponseEntity<FolderResponse> folderResponse = folderController.getMainFolder("qweASD123zxc");

        assertEquals(mainFolder, folderResponse.getBody().getFolder());
        assertEquals(Set.of(), folderResponse.getBody().getSubFolders());
    }

    @Test
    void getFolder_UserNotLoggedIn_ThrowsResponseStatusException() {
        when(authenticationService.isLoggedIn("qweASD123zxc")).thenThrow(IllegalArgumentException.class);

        assertThrows(ResponseStatusException.class,() -> folderController.getFolder("qweASD123zxc", mainFolder.getId()));
    }

    @Test
    void getFolder_DocumentNotOwnedByUser_ThrowsResponseStatusException() {
        when(authenticationService.isLoggedIn("qweASD123zxc")).thenReturn(user.getId());
        when(folderService.getFolder(user.getId(), 1L)).thenThrow(IllegalArgumentException.class);

        assertThrows(ResponseStatusException.class,() -> folderController.getFolder("qweASD123zxc", 1L));
    }

    @Test
    void getFolder_GoodRequest_ReturnsFolder() {
        when(authenticationService.isLoggedIn("qweASD123zxc")).thenReturn(user.getId());
        when(folderService.getFolder(user.getId(), 1L)).thenReturn(mainFolder);
        when(folderService.getSubFolders(user.getId(), mainFolder.getId())).thenReturn(Set.of());

        ResponseEntity<FolderResponse> folderResponse = folderController.getFolder("qweASD123zxc", 1L);

        assertEquals(mainFolder, folderResponse.getBody().getFolder());
        assertEquals(Set.of(), folderResponse.getBody().getSubFolders());
    }

    @Test
    void createFolder_UserNotLoggedIn_ThrowsResponseStatusException() {
        when(authenticationService.isLoggedIn("qweASD123zxc")).thenThrow(IllegalArgumentException.class);

        assertThrows(ResponseStatusException.class,() -> folderController.createFolder("qweASD123zxc", "name", mainFolder.getId()));
    }

    @Test
    void getFolder_ParentDocumentDoesNotExist_ThrowsResponseStatusException() {
        when(authenticationService.isLoggedIn("qweASD123zxc")).thenReturn(user.getId());
        doThrow(IllegalArgumentException.class).when(folderService).createFolder(user.getId(), "name", 1L);

        assertThrows(ResponseStatusException.class,() -> folderController.createFolder("qweASD123zxc", "name",1L));
    }

    @Test
    void getFolder_GoodRequest_ThrowsResponseStatusException() {
        when(authenticationService.isLoggedIn("qweASD123zxc")).thenReturn(user.getId());
        doNothing().when(folderService).createFolder(user.getId(), "name", 1L);

        ResponseEntity<Void> res = folderController.createFolder("qweASD123zxc", "name", 1L);
        assertEquals(HttpStatus.ACCEPTED, res.getStatusCode());
    }
}
