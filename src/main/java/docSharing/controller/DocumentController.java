package docSharing.controller;

import docSharing.Entities.Document;
import docSharing.Entities.Role;
import docSharing.Entities.UpdateMessage;
import docSharing.Entities.User;
import docSharing.service.AuthenticationService;
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
import org.springframework.web.bind.annotation.RequestBody;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        Long userId = authenticationService.isLoggedIn(joinUser.user);
        User user = userService.getUserById(userId);
        String userEmail=user.getEmail();
        Document document = documentService.getDocumentById(joinUser.docId, userId);
        Map<String, Role> usersEmails = documentsViewers.get(joinUser.docId);
        if(usersEmails==null) {
            usersEmails = new HashMap<>();
            Role role = document.getUserRole(user);
            if (role == null) {
                throw new IllegalArgumentException("User does not have access to the document");
            }
            usersEmails.put(userEmail, role);
            documentsViewers.put(joinUser.docId,usersEmails);
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
    public UpdateMessage sendPlainMessage(UpdateMessage message) {
        Long userId = authenticationService.isLoggedIn(message.getUser());
        return documentService.updateContent(message,userId);
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
    public Map<Long,Map<String, Role>> deleteViewer(@Payload Long docId, @Header String token) {
        String userEmail=userService.getUserById(authenticationService.isLoggedIn(token)).getEmail();
        if (documentsViewers.get(docId) == null){
            return documentsViewers;
        }
        documentsViewers.get(docId).remove(userEmail);
        System.out.println("delete viewer!!");
        return documentsViewers;
    }

    /**
     * Class for each user viewing a document
     */
    static class JoinDocument {
        private String user;
        private Long docId;
        public JoinDocument() {
        }
        public void setDocId(Long docId) {
            this.docId = docId;
        }

        public Long getDocId() {
            return docId;
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }
    }
}
