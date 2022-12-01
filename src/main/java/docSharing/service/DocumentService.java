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

    /**
     * create document based on the received parameters and add it to document's table using documentRepository
     * @param userId - id of the owner of the document
     * @param title for the document
     * @param folderId - folder's id to add the document to it
     * @throws ResponseStatusException if the user or the folder not exists, or if the user does not own the folder.
     */
    public void createDocument(Long userId, String title, Long folderId) {
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            throw new IllegalArgumentException(String.format("User with ID: %d doesn't exist", userId));
        }
        Optional<Folder> folder = folderRepository.findById(folderId);
        if (!folder.isPresent()) {
            throw new IllegalArgumentException(String.format("Folder with ID: %d doesn't exist", folderId));
        }
        if (folder.get().getOwner() != user.get()) {
            throw new IllegalArgumentException(String.format("User:%d is not owner of Folder with ID: %d",userId, folderId));
        }

        documentRepository.save(new Document(user.get(), title, folder.get()));
        LOGGER.info("new document added to document's table");
    }
    /**
     * create document based on the received parameters(including the body of the document) and add it to document's table using documentRepository
     * @param userId - id of the owner of the document
     * @param title for the document
     * @param body - the content of the folder
     * @param folderId - folder's id to add the document to it
     * @throws ResponseStatusException if the user or the folder not exists, or if the user does not own the folder.
     */
    public void createDocument(Long userId, String title, String body, Long folderId) {
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            throw new IllegalArgumentException(String.format("User with ID: '%d' doesn't exist", userId));
        }
        Optional<Folder> folder = folderRepository.findById(folderId);
        if (!folder.isPresent()) {
            throw new IllegalArgumentException(String.format("Folder with ID: '%d' doesn't exist", folderId));
        }
        if (folder.get().getOwner() != user.get()) {
            throw new IllegalArgumentException(String.format("User:%d is not owner of Folder with ID: %d",userId, folderId));
        }

        Document document = new Document(user.get(), title, folder.get());
        document.setBody(body);
        documentRepository.save(document);
    }

    /**
     * moves a document into a folder.
     * @param userId the id of the user requesting the move.
     * @param documentId the id of the document to move.
     * @param folderId the id of the folder where to move the document. if this is -1 will move the document one folder closer to root.
     * @throws IllegalArgumentException if the user id, document id or folder id is incorrect,
     * and if the user is not an owner of the document and the folder.
     */
    public void moveDocument(Long userId, Long documentId, Long folderId) {
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            LOGGER.debug(String.format("user:%d doesn't exist", userId));
            throw new IllegalArgumentException(String.format("User with ID: '%d' doesn't exist", userId));
        }
        Optional<Document> document = documentRepository.findById(documentId);
        if (!document.isPresent()) {
            LOGGER.debug(String.format("failing to get document:%d - doesn't exist", documentId));
            throw new IllegalArgumentException(String.format("Document with ID: '%d' doesn't exist", documentId));
        }
        if (folderId == -1L) {
            if (document.get().getFolder().getParentFolder() == null) {
                LOGGER.debug(String.format("failing to move document:%d to parent - folder:%d is a root folder", documentId, folderId));
                throw new IllegalArgumentException(String.format("document:%d is in your root folder", documentId));
            }
            folderId = document.get().getFolder().getParentFolder().getId();
        }
        Optional<Folder> folder = folderRepository.findById(folderId);
        if (!folder.isPresent()) {
            LOGGER.debug(String.format("failing to get folder:%d - doesn't exist", documentId));
            throw new IllegalArgumentException(String.format("folder with ID: '%d' doesn't exist", documentId));
        }

        if (document.get().getOwner() != user.get() || folder.get().getOwner() != user.get()) {
            LOGGER.debug(String.format("move failed - user:%d is not owner of document:%d and/or folder:%d", userId,documentId, folderId));
            throw new IllegalArgumentException(
                    String.format("User with ID: '%d' is not owner of document:%d or folder:%d", userId, documentId, folderId));
        }

        document.get().setFolder(folder.get());
        documentRepository.save(document.get());
        LOGGER.debug(String.format("moved document:%d to folder:%d", documentId, folderId));
    }

    /**
     * update body of document based on the update message.
     * @param update the wanted update.
     * @param userId the user that wants to perform the update.
     * @return the update message after the update is completed. the update may change during the update process.
     * @throws IllegalArgumentException if the user id is incorrect, if the document id is incorrect, or if the user doesn't have editing permissions for the document.
     */
    public UpdateMessage updateContent(UpdateMessage update, Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (!user.isPresent()) {
            LOGGER.debug(String.format("user:%d doesn't exist", userId));
            throw new IllegalArgumentException(String.format("User with ID: '%d' doesn't exist", userId));
        }
        Optional<Document> document = documentRepository.findById(update.getDocumentId());
        if (!document.isPresent()) {
            LOGGER.debug(String.format("failing to get document:%d - doesn't exist", update.getDocumentId()));
            throw new IllegalArgumentException(String.format("Document with ID: '%d' doesn't exist", update.getDocumentId()));
        }
        if (!document.get().getEditors().contains(user.get())) {
            LOGGER.debug(String.format("update failed - user:%d - doesn't have write permissions to document:%d", userId,update.getDocumentId()));
            throw new IllegalArgumentException(
                    String.format("User with ID: '%d' doesn't have Editor permissions in document with ID:' %d'", userId, update.getDocumentId()));
        }

        if (changers.get(update.getDocumentId()) == null) {
            LOGGER.info(String.format("Creating document changer for document:%d", document.get().getId()));
            changers.put(update.getDocumentId(), new DocumentChanger(document.get()));
        }

        LOGGER.debug(String.format("sent change to document changer of document:%d", document.get().getId()));
        return changers.get(update.getDocumentId()).addUpdate(update);
    }

    /**
     * get document by id
     * @param docId
     * @param userId
     * @return the relevant document
     * @throws IllegalArgumentException if the document or the user not exists or if the user haven't permissions for the document
     */
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

    public void shareDocument(Long ownerId, String userEmail, Long docId, Role role) {
        Optional<Document> document = documentRepository.findById(docId);
        if (document.isPresent()) {
            User otherUser = userRepository.findByEmail(userEmail);
            if (otherUser != null) {
                if (role == Role.VIEWER) {
                    document.get().addViewer(otherUser);
                }
                if (role == Role.EDITOR) {
                    document.get().addEditor(otherUser);
                }
                documentRepository.save(document.get());
            }
        }
    }

    /**
     * Helper class for managing the changes to a document.
     * runs two threads in the background: 1)managing changes to a document. 2)to save the document to the database.
     * if the programs stops abruptly, the most data lost is the last three seconds of the program.
     */
    class DocumentChanger {
        private final Document document;
        private final Queue<UpdateMessage> changesQueue = new ConcurrentLinkedQueue<>();

        private final StringBuffer documentBody;

        Thread runnerThread = new Thread(this::runner);

        ScheduledExecutorService saveExecutor = Executors.newScheduledThreadPool(1);

        DocumentChanger(Document document) {
            this.document = document;
            if (document.getBody() == null) document.setBody("");
            documentBody = new StringBuffer(document.getBody());
            saveExecutor.scheduleAtFixedRate(this::saveDocument, 0, 5, TimeUnit.SECONDS);
            LOGGER.debug(String.format("Finished creating document changer for document:%d", document.getId()));
        }

        /**
         * add a message to the queue. this function waits for the update to go through and only then sends it back.
         * @param message the update message with how to change the body of the document.
         * @return the update message sent to the function, possibly modified if needed to solve confilcts.
         */
        public UpdateMessage addUpdate(UpdateMessage message) {
            changesQueue.add(message);
            if (!runnerThread.isAlive()) {
                LOGGER.info(String.format("Executing runner of document changer for document:%d", document.getId()));
                runnerThread = new Thread(this::runner);
                runnerThread.start();
            }
            while (changesQueue.contains(message)) {}
            return message;
        }

        /**
         * This function changes the body of the document and changes the following changes,
         * so that they will appear in the right space in the body.
         * This function will run until the queue of changes has been gone through.
         */
        private void runner() {
            while (changesQueue.peek() != null) {
                UpdateMessage current = changesQueue.peek();
                if (current.getPosition() > documentBody.length()) {
                    LOGGER.debug(String.format("resent update message {documentId:%d, type:%s, content:%s}", document.getId(), current.getType(), current.getContent()));
                    changesQueue.add(current);
                    changesQueue.poll();
                    continue;
                }
                getNewBody(current);
                changeUpcoming(current);
                changesQueue.poll();
            }
            LOGGER.debug(String.format("runner of document:%d saving and stopping, finished queue.", document.getId()));
            document.setBody(documentBody.toString());
            documentRepository.save(document);
        }

        /**
         * this function changes the messages upcoming in the queue in order to fit them in with accordance to the current change.
         * @param current the current message that is being worked on.
         */
        private void changeUpcoming(UpdateMessage current) {
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
                        LOGGER.warn(String.format("Update types added but not supported. tried:%s", current.getType()));
                        throw new UnsupportedOperationException(String.format("%s not recognized update type", current.getType()));
                }
            }
        }

        /**
         * Save the document body to the database.
         * This function will be run every 3 seconds in order to minimize update messages to the db.
         */
        private void saveDocument() {
            if (changesQueue.isEmpty()) return;
            document.setBody(documentBody.toString());
            documentRepository.save(document);
        }

        /**
         * This function changes the body in accordance with the current update message.
         * @param message the update message, regarding how to change the body.
         * @throws UnsupportedOperationException if the message type is not one of {DELETE, DELETE_RANGE, APPEND, APPEND_RANGE}.
         */
        private void getNewBody(UpdateMessage message) {
            switch (message.getType()) {
                case DELETE:
                case DELETE_RANGE:
                    if (documentBody.length() == 0) return;
                    documentBody.delete(message.getPosition(), message.getPosition() + message.getContent().length());
                    break;
                case APPEND:
                case APPEND_RANGE:
                    documentBody.insert(message.getPosition(), message.getContent());
                    break;
                default:
                    LOGGER.warn(String.format("Update types added but not supported. tried:%s", message.getType()));
                    throw new UnsupportedOperationException(String.format("%s not recognized update type", message.getType()));
            }
        }
    }
}
