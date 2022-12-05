package docSharing.controller;

import docSharing.Entities.*;
import docSharing.service.AuthenticationService;
import docSharing.service.ChangeLogService;
import docSharing.service.DocumentService;
import docSharing.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@Controller
public class DocumentController {
    @Autowired
    private DocumentService documentService;
    @Autowired
    private UserService userService;
    private Map<Long, Map<String, Role>> documentsViewers;
    @Autowired
    AuthenticationService authenticationService;
    @Autowired
    private ChangeLogService changeLogService;

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentController.class);

    /**
     * Document's constructor to initialize documentsViewers
     */
    public DocumentController(){
        LOGGER.info("in document controller constructor");
        documentsViewers = new HashMap<>();
    }

    /**
     * Add user to document's viewers
     * @param joinUser
     * @return a Map for all documents and users that viewing it <docId,list<userEmail>>
     * @throws IllegalArgumentException if the user not logged in
     */
    @MessageMapping("/join/")
    @SendTo("/topic/viewers/")
    public Map<Long,Map<String, Role>>  sendJoinMessage(JoinDocument joinUser) {
        Long userId = authenticationService.isLoggedIn(joinUser.getUser());
        User user = userService.getUserById(userId);
        String userEmail=user.getEmail();
        Document document = documentService.getDocumentById(joinUser.getDocId(), userId);
        Map<String, Role> usersEmails = documentsViewers.get(joinUser.getDocId());
        if(usersEmails==null) {
            usersEmails = new HashMap<>();
            Role role = document.getUserRole(user);
            if (role == null) {
                throw new IllegalArgumentException("User does not have access to the document");
            }
            usersEmails.put(userEmail, role);
            documentsViewers.put(joinUser.getDocId(),usersEmails);
        }
        else if (usersEmails.get(userEmail) == null) {
            Role role = document.getUserRole(user);
            if (role == null) {
                throw new IllegalArgumentException("User does not have access to the document");
            }
            usersEmails.put(userEmail, role);
        }
        return documentsViewers;
    }

    /**
     * update the document according to the message received by the request from the client
     * @param message with the updated content
     * @return the same message
     * @throws IllegalAccessException if the user not logged in
     */
    @MessageMapping("/update/")
    @SendTo("/topic/updates/")
    public UpdateResponseMessage sendPlainMessage(UpdateMessage message) {
        LOGGER.info("request from the client to update document's content");
        Long userId = authenticationService.isLoggedIn(message.getUser());
        documentService.updateContent(message, userId);
        String email = authenticationService.getUserByToken(message.getUser()).getEmail();
        UpdateResponseMessage responseMessage = new UpdateResponseMessage(message.getContent(),
                message.getDocumentId(), message.getType(), message.getPosition(), email);
        changeLogService.addLog(responseMessage);
        return responseMessage;
    }

    /**
     * delete user from document viewers set
     * @param docId - the id of the document that the user stop viewing it
     * @param token - user's token
     * @return a Map for all documents and users that viewing it <docId,list<userEmail>>.
     * @throws IllegalAccessException if the user not logged in.
     */
    @MessageMapping("/deleteViewer/")
    @SendTo("/topic/viewers/")
    public Map<Long, Map<String, Role>> deleteViewer(@Payload Long docId, @Header String token) {
        LOGGER.info("Request from the client to delete viewer from document viewer's list");
        String userEmail = userService.getUserById(authenticationService.isLoggedIn(token)).getEmail();
        if (documentsViewers.get(docId) == null){
            return documentsViewers;
        }
        documentsViewers.get(docId).remove(userEmail);
        LOGGER.info(String.format("Viewer with email: %s deleted from viewer's list", userEmail));
        return documentsViewers;
    }
}
