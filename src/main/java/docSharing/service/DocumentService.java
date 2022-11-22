package docSharing.service;

import docSharing.Entities.Document;
import docSharing.Entities.Folder;
import docSharing.Entities.User;
import docSharing.repository.DocumentRepository;
import docSharing.repository.FolderRepository;
import docSharing.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FolderRepository folderRepository;

    public void createDocument(Long userId, String title, Long folderId) {
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("User with ID: \'%d\' doesn't exist", userId));
        }
        Optional<Folder> folder = folderRepository.findById(folderId);
        if (!folder.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Folder with ID: \'%d\' doesn't exist", folderId));
        }
        documentRepository.save(new Document(user.get(), title, folder.get()));
    }

    public void updateContent(Long docId, Long userId, String content) throws
            IllegalAccessException, IllegalArgumentException {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            Optional<Document> document = documentRepository.findById(docId);
            if (document.isPresent()) {
                if (document.get().getEditors().contains(user.get())) {
                    document.get().setBody(content);
                    documentRepository.save(document.get());
                } else {
                    throw new IllegalAccessException(
                            String.format("User with ID: \'%d\' doesn't have " +
                                    "Editor permissions in document with ID:\' %d\'", userId, docId));
                }
            } else {
                throw new IllegalArgumentException(
                        String.format("Document with ID: \'%d\' not found", docId));
            }
        } else {
            throw new IllegalArgumentException(
                    String.format("User with ID: \'%d\' not found", userId));
        }
    }
}
