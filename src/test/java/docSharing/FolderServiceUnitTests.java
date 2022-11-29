package docSharing;

import docSharing.Entities.Folder;
import docSharing.Entities.User;
import docSharing.repository.FolderRepository;
import docSharing.repository.UserRepository;
import docSharing.service.FolderService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@SpringBootTest
public class FolderServiceUnitTests {
    @Autowired
    FolderService folderService;

    @MockBean
    FolderRepository folderRepository;

    @MockBean
    UserRepository userRepository;

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
    void getMainFolder_MainFolderIsPresent_returnsMainFolder() {
        when(folderRepository.findByOwnerIdAndParentFolderIsNull(user.getId())).thenReturn(Optional.of(mainFolder));

        assertEquals(mainFolder, folderService.getMainFolder(user.getId()));
    }

    @Test
    void getMainFolder_UserDoesNotExist_throwsIllegalArgument() {
        when(folderRepository.findByOwnerIdAndParentFolderIsNull(user.getId())).thenReturn(Optional.empty());
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> folderService.getMainFolder(user.getId()));
    }

    @Test
    void getMainFolder_MainFolderDoesNotExist_returnsNewFolder() {
        when(folderRepository.findByOwnerIdAndParentFolderIsNull(user.getId())).thenReturn(Optional.empty());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        Folder newFolder = folderService.getMainFolder(user.getId());

        assertEquals(newFolder.getOwner(), user);
    }

    @Test
    void getFolder_FolderIsNotPresent_throwsIllegalArgument() {
        when(folderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> folderService.getFolder(user.getId(), 1L));
    }

    @Test
    void getFolder_FolderNotOwnedByUser_throwsIllegalArgument() {
        User newUser = new User();
        newUser.setId(6L);
        when(folderRepository.findById(1L)).thenReturn(Optional.of(new Folder(newUser)));

        assertThrows(IllegalArgumentException.class, () -> folderService.getFolder(user.getId(), 1L));
    }

    @Test
    void getFolder_FolderExistsAndOwnedByUser_returnsFolder() {
        when(folderRepository.findById(1L)).thenReturn(Optional.of(mainFolder));

        assertEquals(mainFolder, folderService.getFolder(user.getId(), 1L));
    }

    @Test
    void getSubFolder_FolderDoesNotExist_returnsEmptySet() {
        when(folderRepository.findByParentFolderIdAndOwnerId(1L, user.getId())).thenReturn(Set.of());

        assertEquals(Set.of(), folderService.getSubFolders(user.getId(), 1L));
    }

    @Test
    void getSubFolder_FolderWithOneSubFolder_returnsSetOfSizeOne() {
        when(folderRepository.findByParentFolderIdAndOwnerId(1L, user.getId())).thenReturn(Set.of(new Folder()));

        assertEquals(1, folderService.getSubFolders(user.getId(), 1L).size());
    }

    @Test
    void createFolder_NotRegisteredUser_throwsIllegalArgument() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> folderService.createFolder(user.getId(), "fail", mainFolder.getId()));
    }

    @Test
    void createFolder_ParentFolderDoesNotExist_throwsIllegalArgument() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(folderRepository.findById(mainFolder.getId())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> folderService.createFolder(user.getId(), "fail", mainFolder.getId()));
    }

    @Test
    void createFolder_NewFolder_doesNotThrow() {
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(folderRepository.findById(mainFolder.getId())).thenReturn(Optional.of(mainFolder));

        assertDoesNotThrow(() -> folderService.createFolder(user.getId(), "fail", mainFolder.getId()));
    }
}
