package docSharing.service;

import docSharing.Entities.Document;
import docSharing.Entities.User;
import docSharing.repository.DocumentRepository;
import docSharing.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.Set;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    public void createDocument(Long userId, String title) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            documentRepository.save(new Document(user.get(), title));
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("User with ID: \'%d\' doesn't exist", userId));
        }
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

    public Document getDocumentById(Long docId, Long userId) {
        Optional<Document> OptDoc = documentRepository.findById(docId);
        Optional<User> OptUser = userRepository.findById(userId);
        if (!OptUser.isPresent()) {
            throw new IllegalArgumentException(String.format("User with ID: '%d' not found", userId));
        }
        if (!OptDoc.isPresent()) {
            throw new IllegalArgumentException(String.format("Document with ID: '%d' not found", userId));
        }

        Document doc = OptDoc.get();
        User user = OptUser.get();

        if (!doc.getEditors().contains(user) && !doc.getViewers().contains(user)) {
            throw new IllegalArgumentException("User doesn't have access to the document");
        }

        return doc;
    }

    public Set<Document> getAllDocuments(Long id) throws IllegalArgumentException {
        Optional<User> user = userRepository.findById(id);
        if (!user.isPresent()) {
            throw new IllegalArgumentException(String.format("User with ID: '%d' not found", id));
        }

        return user.get().getAllDocuments();
    }
}
