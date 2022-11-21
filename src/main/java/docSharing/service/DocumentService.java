package docSharing.service;

import docSharing.Entities.Document;
import docSharing.Entities.User;
import docSharing.repository.DocumentRepository;
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

    public void createDocument(Long userId, String title) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            documentRepository.save(new Document(user.get(), title));
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("User with ID: \'%d\' doesn't exist", userId));
        }
    }

    public void updateContent(Long docId, Long userId, String content) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            Optional<Document> document = documentRepository.findById(docId);
            if (document.isPresent()) {
                if (document.get().getEditors().contains(user.get())) {
                    document.get().setBody(content);
                    documentRepository.save(document.get());
                } else {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            String.format("User with ID: \'%d\' doesn't have " +
                                    "Editor permissions in document with ID:\' %d\'", userId, docId));
                }
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                        String.format("Document with ID: \'%d\' not found", docId));
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    String.format("User with ID: \'%d\' not found", userId));
        }
    }
}
