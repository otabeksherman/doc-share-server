package docSharing.controller;

import docSharing.service.AuthenticationService;
import docSharing.service.DocumentService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Controller
public class DocumentController {
    @Autowired
    private DocumentService documentService;
    private Map<Long,UpdateMessage> currentState;
    @Autowired
    private SimpMessagingTemplate simpMessage;
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
        //ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        //executor.scheduleAtFixedRate(updateDocumentBody, 0, 15, TimeUnit.SECONDS);
    }
    @MessageMapping("/join")
    public void sendPlainMessage(JoinMessage message) {
        System.out.println(message.user + " joined");
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
        //simpMessage.convertAndSend("/topic/updates/" + message.documentId, message);

        return message;
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

    private class JoinMessage {
        private String user;

        public JoinMessage() {
        }

        public String getUser() {
            return user;
        }

        public void setUser(String user) {
            this.user = user;
        }
    }
}
