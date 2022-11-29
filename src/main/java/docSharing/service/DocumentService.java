package docSharing.service;

import docSharing.Entities.*;
import docSharing.repository.DocumentRepository;
import docSharing.repository.FolderRepository;
import docSharing.repository.UserRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FolderRepository folderRepository;

    private static final Logger LOGGER = LogManager.getLogger(DocumentService.class);

    private static final Map<Long, DocumentChanger> changers = new HashMap<>();

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

    public void createDocument(Long userId, String title, String body, Long folderId) {
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("User with ID: '%d' doesn't exist", userId));
        }
        Optional<Folder> folder = folderRepository.findById(folderId);
        if (!folder.isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    String.format("Folder with ID: '%d' doesn't exist", folderId));
        }
        Document document = new Document(user.get(), title, folder.get());
        document.setBody(body);
        documentRepository.save(document);
    }

    public UpdateMessage updateContent(UpdateMessage update, Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            throw new IllegalArgumentException(String.format("User with ID: '%d' doesn't exist", userId));
        }
        Optional<Document> document = documentRepository.findById(update.getDocumentId());
        if (!document.isPresent()) {
            throw new IllegalArgumentException(String.format("Document with ID: '%d' doesn't exist", update.getDocumentId()));
        }
        if (!document.get().getEditors().contains(user.get())) {
            throw new IllegalArgumentException(
                    String.format("User with ID: '%d' doesn't have Editor permissions in document with ID:' %d'", userId, update.getDocumentId()));
        }

        if (changers.get(update.getDocumentId()) == null) {
            changers.put(update.getDocumentId(), new DocumentChanger(document.get()));
        }

        return changers.get(update.getDocumentId()).addUpdate(update);
    }

    public Document getDocumentById(Long docId, Long userId) {
        Optional<Document> optDoc = documentRepository.findById(docId);
        Optional<User> optUser = userRepository.findById(userId);
        if (!optUser.isPresent()) {
            throw new IllegalArgumentException(String.format("User with ID: '%d' not found", userId));
        }
        if (!optDoc.isPresent()) {
            throw new IllegalArgumentException(String.format("Document with ID: '%d' not found", userId));
        }

        Document doc = optDoc.get();
        User user = optUser.get();

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

    class DocumentChanger {
        private Document document;
        private final Queue<UpdateMessage> changesQueue = new ConcurrentLinkedQueue<>();

        Thread runnerThread = new Thread(this::runner);

        ScheduledExecutorService saveExecutor = Executors.newScheduledThreadPool(1);

        DocumentChanger(Document document) {
            this.document = document;
            saveExecutor.scheduleAtFixedRate(this::saveDocument, 0, 5, TimeUnit.SECONDS);
        }

        public Document getDocument() {
            return document;
        }

        public UpdateMessage addUpdate(UpdateMessage message) {
            changesQueue.add(message);
            if (!runnerThread.isAlive()) {
                runnerThread = new Thread(this::runner);
                runnerThread.start();
            }
            while (changesQueue.contains(message)) {}
            return message;
        }

        private void runner() {
            while (changesQueue.peek() != null) {
                UpdateMessage current = changesQueue.peek();
                String body = document.getBody();
                if (current.getPosition() > body.length()) {
                    changesQueue.add(current);
                    changesQueue.poll();
                    continue;
                }
                body = getNewBody(body, current);
                document.setBody(body);
                changePrevious(current);
                changesQueue.poll();
            }
            documentRepository.save(document);
        }

        private void changePrevious(UpdateMessage current) {
            for (UpdateMessage laterMessage : changesQueue) {
                if (Objects.equals(laterMessage.getUser(), current.getUser()) || laterMessage.getPosition() < current.getPosition()) {
                    continue;
                }

                switch (current.getType()) {
                    case DELETE:
                    case DELETE_RANGE:
                        laterMessage.setPosition(laterMessage.getPosition() - current.getContent().length());
                        break;
                    case APPEND:
                    case APPEND_RANGE:
                        laterMessage.setPosition(laterMessage.getPosition() + current.getContent().length());
                        break;
                    default:
                        throw new UnsupportedOperationException(String.format("%s not recognized update type", current.getType()));
                }
            }
        }

        private void saveDocument() {
            if (changesQueue.isEmpty()) return;
            documentRepository.save(document);
        }

        private String getNewBody(String oldBody, UpdateMessage message) {
            String body;
            switch (message.getType()) {
                case DELETE:
                    if (oldBody.length() == 0) return "";
                    body = oldBody.substring(0, message.getPosition()) + oldBody.substring(message.getPosition() + 1);
                    break;
                case APPEND:
                case APPEND_RANGE:
                    body = oldBody.substring(0, message.getPosition()) + message.getContent() + oldBody.substring(message.getPosition());
                    break;
                case DELETE_RANGE:
                    if (oldBody.length() == 0) return "";
                    body = oldBody.substring(0, message.getPosition()) + oldBody.substring(message.getPosition() + message.getContent().length());
                    break;
                default:
                    throw new UnsupportedOperationException(String.format("%s not recognized update type", message.getType()));
            }
            return body;
        }
    }
}
