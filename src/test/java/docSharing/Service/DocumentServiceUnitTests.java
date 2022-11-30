package docSharing.Service;

import docSharing.Entities.*;
import docSharing.repository.DocumentRepository;
import docSharing.repository.FolderRepository;
import docSharing.repository.UserRepository;
import docSharing.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class DocumentServiceUnitTests {

    @Autowired
    DocumentService documentService;

    @MockBean
    UserRepository userRepository;

    @MockBean
    FolderRepository folderRepository;

    @MockBean
    DocumentRepository documentRepository;

    User user;
    Folder mainFolder;
    Document document;
    UpdateMessage updateMessage;
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
        document.setId(99L);
        updateMessage = new UpdateMessage();
        updateMessage.setDocumentId(document.getId());
    }

    @Test
    void createDocument_UserIdIncorrect_throwIllegalArgumentException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> documentService.createDocument(user.getId(), "title", mainFolder.getId()));
    }

    @Test
    void createDocument_FolderIdIncorrect_throwIllegalArgumentException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(folderRepository.findById(mainFolder.getId())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> documentService.createDocument(user.getId(), "title", mainFolder.getId()));
    }

    @Test
    void createDocument_FolderNotOwnedByUser_throwsIllegalArgumentException() {
        User secondUser = new User();
        secondUser.setId(50L);
        mainFolder = new Folder(secondUser);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(folderRepository.findById(mainFolder.getId())).thenReturn(Optional.of(mainFolder));

        assertThrows(IllegalArgumentException.class, () -> documentService.createDocument(user.getId(), "title", mainFolder.getId()));
    }

    @Test
    void createDocument_GoodRequest_doesNotThrow() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(folderRepository.findById(mainFolder.getId())).thenReturn(Optional.of(mainFolder));

        assertDoesNotThrow(() -> documentService.createDocument(user.getId(), "title", mainFolder.getId()));
    }

    @Test
    void createDocumentWithBody_UserIdIncorrect_throwIllegalArgumentException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> documentService.createDocument(user.getId(), "title", "body", mainFolder.getId()));
    }

    @Test
    void createDocumentWithBody_FolderIdIncorrect_throwIllegalArgumentException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(folderRepository.findById(mainFolder.getId())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> documentService.createDocument(user.getId(), "title", "body", mainFolder.getId()));
    }

    @Test
    void createDocumentWithBody_FolderNotOwnedByUser_throwsIllegalArgumentException() {
        User secondUser = new User();
        secondUser.setId(50L);
        mainFolder = new Folder(secondUser);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(folderRepository.findById(mainFolder.getId())).thenReturn(Optional.of(mainFolder));

        assertThrows(IllegalArgumentException.class, () -> documentService.createDocument(user.getId(), "title", "body", mainFolder.getId()));
    }

    @Test
    void createDocumentWithBody_GoodRequest_doesNotThrow() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(folderRepository.findById(mainFolder.getId())).thenReturn(Optional.of(mainFolder));

        assertDoesNotThrow(() -> documentService.createDocument(user.getId(), "title", "body", mainFolder.getId()));
    }

    @Test
    void moveDocument_UserIdIncorrect_throwIllegalArgumentException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> documentService.moveDocument(user.getId(), document.getId(), mainFolder.getId()));
    }

    @Test
    void moveDocument_documentIdIncorrect_throwIllegalArgumentException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(documentRepository.findById(document.getId())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> documentService.moveDocument(user.getId(), document.getId(), mainFolder.getId()));
    }

    @Test
    void moveDocument_FolderIdIncorrect_throwIllegalArgumentException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(documentRepository.findById(document.getId())).thenReturn(Optional.of(document));
        when(folderRepository.findById(mainFolder.getId())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> documentService.moveDocument(user.getId(), document.getId(), mainFolder.getId()));
    }

    @Test
    void moveDocument_FolderNotOwnedByUser_throwIllegalArgumentException() {
        User secondUser = new User();
        secondUser.setId(50L);
        mainFolder = new Folder(secondUser);
        mainFolder.setId(1L);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(documentRepository.findById(document.getId())).thenReturn(Optional.of(document));
        when(folderRepository.findById(mainFolder.getId())).thenReturn(Optional.of(mainFolder));

        assertThrows(IllegalArgumentException.class, () -> documentService.moveDocument(user.getId(), document.getId(), mainFolder.getId()));
    }

    @Test
    void moveDocument_DocumentNotOwnedByUser_throwIllegalArgumentException() {
        User secondUser = new User();
        secondUser.setId(50L);
        document = new Document(secondUser, "title", mainFolder);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(documentRepository.findById(document.getId())).thenReturn(Optional.of(document));
        when(folderRepository.findById(mainFolder.getId())).thenReturn(Optional.of(mainFolder));

        assertThrows(IllegalArgumentException.class, () -> documentService.moveDocument(user.getId(), document.getId(), mainFolder.getId()));
    }

    @Test
    void moveDocument_MoveCloserToRootButAlreadyInRoot_throwIllegalArgumentException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(documentRepository.findById(document.getId())).thenReturn(Optional.of(document));
        when(folderRepository.findById(mainFolder.getId())).thenReturn(Optional.of(mainFolder));

        assertThrows(IllegalArgumentException.class, () -> documentService.moveDocument(user.getId(), document.getId(), -1L));
    }

    @Test
    void moveDocument_MoveCloserToRoot_movesToCorrectFolder() {
        Folder secondFolder = new Folder(user);
        secondFolder.setParentFolder(mainFolder);
        document.setFolder(secondFolder);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(documentRepository.findById(document.getId())).thenReturn(Optional.of(document));
        when(folderRepository.findById(mainFolder.getId())).thenReturn(Optional.of(mainFolder));

        documentService.moveDocument(user.getId(), document.getId(), -1L);

        assertEquals(mainFolder, document.getFolder());
    }

    @Test
    void moveDocument_MoveToFolder_movesToCorrectFolder() {
        Folder secondFolder = new Folder(user);
        secondFolder.setId(45L);
        secondFolder.setParentFolder(mainFolder);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(documentRepository.findById(document.getId())).thenReturn(Optional.of(document));
        when(folderRepository.findById(secondFolder.getId())).thenReturn(Optional.of(secondFolder));

        documentService.moveDocument(user.getId(), document.getId(), secondFolder.getId());

        assertEquals(secondFolder, document.getFolder());
    }

    @Test
    void updateDocument_UserIdIncorrect_throwIllegalArgumentException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> documentService.updateContent(updateMessage, user.getId()));
    }

    @Test
    void updateDocument_documentIdIncorrect_throwIllegalArgumentException() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(documentRepository.findById(document.getId())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> documentService.updateContent(updateMessage, user.getId()));
    }

    @Test
    void updateDocument_UserNotEditorForDocument_throwIllegalArgumentException() {
        User secondUser = new User();
        secondUser.setId(50L);
        document = new Document(secondUser, "title", mainFolder);
        document.setId(5L);
        updateMessage.setDocumentId(document.getId());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(documentRepository.findById(document.getId())).thenReturn(Optional.of(document));
        when(folderRepository.findById(mainFolder.getId())).thenReturn(Optional.of(mainFolder));

        assertThrows(IllegalArgumentException.class, () -> documentService.updateContent(updateMessage, user.getId()));
    }

    @Test
    void updateDocument_AppendRange_bodyChanged() throws InterruptedException {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(documentRepository.findById(document.getId())).thenReturn(Optional.of(document));
        when(folderRepository.findById(mainFolder.getId())).thenReturn(Optional.of(mainFolder));
        updateMessage.setContent("hello");
        updateMessage.setType(UpdateType.APPEND_RANGE);

        documentService.updateContent(updateMessage, user.getId());
        Thread.sleep(20);
        assertEquals("hello", document.getBody());

        eraseDocument(document);
    }

    @Test
    void updateDocument_Append_bodyChanged() throws InterruptedException {
        Document appendDocument = new Document(user, "append", mainFolder);
        appendDocument.setId(4L);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(documentRepository.findById(appendDocument.getId())).thenReturn(Optional.of(appendDocument));
        when(folderRepository.findById(mainFolder.getId())).thenReturn(Optional.of(mainFolder));

        updateMessage.setDocumentId(appendDocument.getId());
        updateMessage.setContent("h");
        updateMessage.setType(UpdateType.APPEND);

        documentService.updateContent(updateMessage, user.getId());
        Thread.sleep(20);
        assertEquals("h", appendDocument.getBody());

        eraseDocument(appendDocument);
    }

    @Test
    void updateDocument_Delete_bodyChanged() throws InterruptedException {
        Document deleteDocument = new Document(user, "append", mainFolder);
        deleteDocument.setId(5L);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(documentRepository.findById(deleteDocument.getId())).thenReturn(Optional.of(deleteDocument));
        when(folderRepository.findById(mainFolder.getId())).thenReturn(Optional.of(mainFolder));
        deleteDocument.setBody("hellyo");

        updateMessage.setDocumentId(deleteDocument.getId());
        updateMessage.setContent("y");
        updateMessage.setType(UpdateType.DELETE);
        updateMessage.setPosition(4);

        documentService.updateContent(updateMessage, user.getId());
        Thread.sleep(20);
        assertEquals("hello", deleteDocument.getBody());

        eraseDocument(deleteDocument);
    }

    @Test
    void updateDocument_DeleteRange_bodyChanged() throws InterruptedException {
        Document deleteRangeDocument = new Document(user, "append", mainFolder);
        deleteRangeDocument.setId(6L);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(documentRepository.findById(deleteRangeDocument.getId())).thenReturn(Optional.of(deleteRangeDocument));
        when(folderRepository.findById(mainFolder.getId())).thenReturn(Optional.of(mainFolder));
        deleteRangeDocument.setBody("helhellolo");

        updateMessage.setDocumentId(deleteRangeDocument.getId());
        updateMessage.setContent("hello");
        updateMessage.setType(UpdateType.DELETE_RANGE);
        updateMessage.setPosition(3);

        documentService.updateContent(updateMessage, user.getId());
        Thread.sleep(20);
        assertEquals("hello", deleteRangeDocument.getBody());

        eraseDocument(deleteRangeDocument);
    }

    private void eraseDocument(Document doc) throws InterruptedException {
        UpdateMessage removeAll = new UpdateMessage();
        removeAll.setDocumentId(doc.getId());
        removeAll.setContent(doc.getBody());
        removeAll.setType(UpdateType.DELETE_RANGE);
        removeAll.setPosition(0);

        documentService.updateContent(removeAll, user.getId());
        Thread.sleep(50L);
    }
}
