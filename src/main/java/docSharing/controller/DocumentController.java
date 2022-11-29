package docSharing.controller;

import docSharing.Entities.Document;
import docSharing.Entities.User;
import docSharing.service.AuthenticationService;
import docSharing.service.DocumentService;
import docSharing.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Controller
public class DocumentController {
    @Autowired
    private DocumentService documentService;
    @Autowired
    private UserService userService;
    private Map<Long,UpdateMessage> currentState;
    private Map<Long, List<String>> documentsViewers;
    @Autowired
    AuthenticationService authenticationService;

    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentController.class);
    Runnable updateDocumentBody = new Runnable() {
        public void run() {
            if(currentState!=null)
                for (UpdateMessage message:
                     currentState.values()) {
                    try {
                        documentService.updateContent(message.documentId, 1L,message.content);
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
        }
    };
    public DocumentController(){
        LOGGER.info("in document controller constructor");
        currentState = new HashMap<>();
        documentsViewers = new HashMap<>();
        //ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        //executor.scheduleAtFixedRate(updateDocumentBody, 0, 15, TimeUnit.SECONDS);
    }
    @MessageMapping("/join/")
    @SendTo("/topic/viewers/")
    public Map<Long,List<String>> sendPlainMessage(JoinDocument joinUser) {
        String userEmail=userService.getUserById(authenticationService.isLoggedIn(joinUser.user)).getEmail();
        List<String> usersEmails = documentsViewers.get(joinUser.docId);
        if(usersEmails==null){
            usersEmails = new ArrayList<>();
            usersEmails.add(userEmail);
            documentsViewers.put(joinUser.docId,usersEmails);
        }
        else{
            if(!usersEmails.contains(userEmail))
                usersEmails.add(userEmail);
        }
        System.out.println(userEmail + " joined");
        return documentsViewers;
    }

    @MessageMapping("/update/")
    @SendTo("/topic/updates/")
    public UpdateMessage sendPlainMessage(UpdateMessage message) throws IllegalAccessException {
        /*if(currentState.get(message.documentId)!=null)
            currentState.replace(message.documentId, message);
        else
            currentState.put(message.documentId, message);*/
        Long userId = authenticationService.isLoggedIn(message.user);
        documentService.updateContent(message.documentId,userId,message.content);
        return message;
    }
    @GetMapping("/viewers/")
    public ResponseEntity<Map<Long, List<String>>> getDocumentById() {
        try {
            return new ResponseEntity<>(documentsViewers, HttpStatus.OK);
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "no viewers");
        }
    }
    @MessageMapping("/deleteViewer/")
    @SendTo("/topic/viewers/")
    public Map<Long,List<String>> deleteViewer(Long docId, String token) {
        String userEmail=userService.getUserById(authenticationService.isLoggedIn(token)).getEmail();
        documentsViewers.get(docId).remove(userEmail);
        System.out.println("delete viewer!!");
        return documentsViewers;
    }
    static class UpdateMessage {
        private String user;
        private String content;
        private Long documentId;
        private UpdateType type;
        private int position;
        public UpdateMessage() {
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }

        public String getContent() {
            return content;
        }

        public Long getDocumentId() {
            return documentId;
        }


        public void setContent(String content) {
            this.content = content;
        }

        public void setDocumentId(Long documentId) {
            this.documentId = documentId;
        }
        public UpdateType getType() {
            return type;
        }
        public void setType(UpdateType type) {
            this.type = type;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }
        @Override
        public String toString() {
            return "UpdateMessage{" +
                    "userId=" + user +
                    ", content='" + content + '\'' +
                    ", documentId=" + documentId +
                    '}';
        }
    }

    public enum UpdateType{
        DELETE,
        APPEND,
        DELETE_RANGE,
        APPEND_RANGE
    }

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
