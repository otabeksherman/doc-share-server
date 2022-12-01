package docSharing.controller;

import docSharing.Entities.*;
import docSharing.service.AuthenticationService;
import docSharing.service.DocumentService;
import docSharing.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class DocumentControllerUnitTests {

    @Autowired
    DocumentController documentController;

    @MockBean
    AuthenticationService authenticationService;

    @MockBean
    UserService userService;

    @MockBean
    DocumentService documentService;

    User user;
    Folder mainFolder;

    Document document;

    DocumentController.JoinDocument joinDocument;

    @BeforeEach
    void setup() {
        user = new User();
        user.setEmail("gideon.jaffe@gmail.com");
        user.setPassword("qwer1234");
        user.setId(5L);
        user.setActivated(true);

        mainFolder = new Folder(user);

        document = new Document(user, "title", mainFolder);
        document.setId(2L);

        joinDocument = new DocumentController.JoinDocument();
        joinDocument.setUser("qweASD123zxc");
        joinDocument.setDocId(document.getId());

    }

    @Test
    void sendJoinMessage_TokenInvalid_throwsIllegalArgumentException() {
        when(authenticationService.isLoggedIn("qweASD123zxc")).thenThrow(IllegalArgumentException.class);

        assertThrows(IllegalArgumentException.class, () -> documentController.sendJoinMessage(joinDocument));
    }

    @Test
    void sendJoinMessage_SingleUser_returnsArraySizeOne() {
        when(authenticationService.isLoggedIn("qweASD123zxc")).thenReturn(user.getId());
        when(userService.getUserById(user.getId())).thenReturn(user);

        Map<String, Role> viewers = documentController.sendJoinMessage(joinDocument).get(joinDocument.getDocId());

        assertEquals(1, viewers.size());
        assertTrue(viewers.containsKey(user.getEmail()));

        documentController.deleteViewer(joinDocument.getDocId(), "qweASD123zxc");
    }

    @Test
    void sendJoinMessage_ThreeUsers_returnsArraySizeThree() {
        when(authenticationService.isLoggedIn("qweASD123zxc")).thenReturn(user.getId());
        when(userService.getUserById(user.getId())).thenReturn(user, createUser("gideon@gmail.com"), createUser("figglophobia@gmail.com"),
                                                                user, createUser("gideon@gmail.com"), createUser("figglophobia@gmail.com"));

        documentController.sendJoinMessage(joinDocument).get(joinDocument.getDocId());
        documentController.sendJoinMessage(joinDocument).get(joinDocument.getDocId());
        Map<String, Role> viewers = documentController.sendJoinMessage(joinDocument).get(joinDocument.getDocId());


        assertEquals(3, viewers.size());
        Object[] usersList = Stream.of(user, createUser("gideon@gmail.com"), createUser("figglophobia@gmail.com")).map(User::getEmail).toArray();
        assertArrayEquals(usersList, viewers.values().toArray());

        documentController.deleteViewer(joinDocument.getDocId(), "qweASD123zxc");
        documentController.deleteViewer(joinDocument.getDocId(), "qweASD123zxc");
        documentController.deleteViewer(joinDocument.getDocId(), "qweASD123zxc");
    }

    private User createUser(String email) {
        User newUser = new User();
        newUser.setEmail(email);
        return newUser;
    }

    @Test
    void sendJoinMessage_SingleUserTwice_returnsArraySizeOne() {
        when(authenticationService.isLoggedIn("qweASD123zxc")).thenReturn(user.getId());
        when(userService.getUserById(user.getId())).thenReturn(user);

        documentController.sendJoinMessage(joinDocument).get(joinDocument.getDocId());
        Map<String, Role> viewers = documentController.sendJoinMessage(joinDocument).get(joinDocument.getDocId());

        assertEquals(1, viewers.size());
        assertTrue(viewers.containsKey(user.getEmail()));

        documentController.deleteViewer(joinDocument.getDocId(), "qweASD123zxc");
    }

    @Test
    void sendPlainMessage_InvalidToken_throwsIllegalArgumentException() {
        when(authenticationService.isLoggedIn("qweASD123zxc")).thenThrow(IllegalArgumentException.class);
        UpdateMessage updateMessage = new UpdateMessage();
        updateMessage.setUser("qweASD123zxc");

        assertThrows(IllegalArgumentException.class, () -> documentController.sendPlainMessage(updateMessage));
    }

    @Test
    void sendPlainMessage_GoodRequest_returnUpdateMessage() {
        when(authenticationService.isLoggedIn("qweASD123zxc")).thenReturn(user.getId());
        UpdateMessage updateMessage = new UpdateMessage();
        updateMessage.setUser("qweASD123zxc");
        updateMessage.setType(UpdateType.APPEND);
        when(documentService.updateContent(updateMessage, user.getId())).thenReturn(updateMessage);

        UpdateMessage updateReturned = documentController.sendPlainMessage(updateMessage);

        assertEquals(updateMessage.getType(), updateReturned.getType());
    }

    @Test
    void deleteViewer_InvalidToken_ThrowsIllegalArgumentException() {
        when(authenticationService.isLoggedIn("qweASD123zxc")).thenThrow(IllegalArgumentException.class);

        assertThrows(IllegalArgumentException.class, () -> documentController.deleteViewer(document.getId(), "qweASD123zxc"));
    }

    @Test
    void deleteViewer_NotViewing_DoesNotThrow() {
        when(authenticationService.isLoggedIn("qweASD123zxc")).thenReturn(user.getId());
        when(userService.getUserById(user.getId())).thenReturn(user);

        assertDoesNotThrow(() -> documentController.deleteViewer(document.getId(), "qweASD123zxc"));
    }

    @Test
    void deleteViewer_ViewingAlone_NoViewers() {
        when(authenticationService.isLoggedIn("qweASD123zxc")).thenReturn(user.getId());
        when(userService.getUserById(user.getId())).thenReturn(user);
        documentController.sendJoinMessage(joinDocument);
        Map<String, Role> viewers = documentController.deleteViewer(document.getId(), "qweASD123zxc").get(document.getId());

        assertEquals(Map.of(), viewers);
    }

    @Test
    void deleteViewer_ViewingWithMoreUsers_NoLongerViewing() {
        when(authenticationService.isLoggedIn("qweASD123zxc")).thenReturn(user.getId());
        when(userService.getUserById(user.getId())).thenReturn(user, createUser("gideon@gmail.com"), createUser("figglophobia@gmail.com"),
                                                                user, createUser("gideon@gmail.com"), createUser("figglophobia@gmail.com"));
        documentController.sendJoinMessage(joinDocument);
        documentController.sendJoinMessage(joinDocument);
        documentController.sendJoinMessage(joinDocument);

        Map<String, Role> viewers = documentController.deleteViewer(document.getId(), "qweASD123zxc").get(document.getId());
        Object[] usersList = Stream.of(createUser("gideon@gmail.com"), createUser("figglophobia@gmail.com")).map(User::getEmail).toArray();
        assertArrayEquals(usersList, viewers.values().toArray());

        documentController.deleteViewer(joinDocument.getDocId(), "qweASD123zxc");
        documentController.deleteViewer(joinDocument.getDocId(), "qweASD123zxc");
    }
}
