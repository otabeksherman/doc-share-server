package docSharing.controller;

import docSharing.Entities.UpdateMessage;
import docSharing.service.AuthenticationService;
import docSharing.service.DocumentService;
import docSharing.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    private Map<Long, List<String>> documentsViewers;
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
    public Map<Long,List<String>> sendPlainMessage(JoinDocument joinUser) {
        LOGGER.info(String.format("Client send the joinDocument: %s",joinUser));
        String userEmail=userService.getUserById(authenticationService.isLoggedIn(joinUser.user)).getEmail();
        List<String> usersEmails = documentsViewers.get(joinUser.docId);
        if(usersEmails==null){
            LOGGER.info(String.format("There is no viewers for document with id: %l",joinUser.docId));
            usersEmails = new ArrayList<>();
            usersEmails.add(userEmail);
            documentsViewers.put(joinUser.docId,usersEmails);
            LOGGER.info(String.format("Add user to viewers list of the document with id: %l",joinUser.docId));
        }
        else{
            if(!usersEmails.contains(userEmail)) {
                LOGGER.info(String.format("The user does not exist in viewers list of the document with id: %l", joinUser.docId));
                usersEmails.add(userEmail);
            }
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
    public UpdateMessage sendPlainMessage(UpdateMessage message) throws IllegalAccessException {
        LOGGER.info("request from the client to update document's content");
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
    public Map<Long,List<String>> deleteViewer(Long docId, String token) {
        LOGGER.info("Request from the client to delete viewer from document viewer's list");
        String userEmail=userService.getUserById(authenticationService.isLoggedIn(token)).getEmail();
        documentsViewers.get(docId).remove(userEmail);
        LOGGER.info(String.format("Viewer with email: %s deleted from viewer's list",userEmail));
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
