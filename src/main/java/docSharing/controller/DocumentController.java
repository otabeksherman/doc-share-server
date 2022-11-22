package docSharing.controller;

import docSharing.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
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
    Runnable updateDocumentBody = new Runnable() {
        public void run() {
            if(currentState!=null)
                for (UpdateMessage message:
                     currentState.values()) {
                     documentService.updateContent(message.documentId,message.userId,message.content);
                }
        }
    };
    public DocumentController(){
        currentState = new HashMap<>();
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(updateDocumentBody, 0, 15, TimeUnit.SECONDS);
    }
    @MessageMapping("/join")
    public void sendPlainMessage(JoinMessage message) {
        System.out.println(message.user + " joined");
    }

    @RequestMapping("/document/update")
    public ResponseEntity<String> sendPlainMessage(@RequestBody UpdateMessage message){
        if(currentState.get(message.documentId)!=null)
            currentState.replace(message.documentId, message);
        else
            currentState.put(message.documentId, message);
        return new ResponseEntity<>("message updated", HttpStatus.OK);
    }

    static class UpdateMessage {
        private Long userId;
        private String content;
        private Long documentId;

        public UpdateMessage() {
        }

        public Long getUserId() {
            return userId;
        }

        public String getContent() {
            return content;
        }

        public Long getDocumentId() {
            return documentId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public void setDocumentId(Long documentId) {
            this.documentId = documentId;
        }

        @Override
        public String toString() {
            return "UpdateMessage{" +
                    "userId=" + userId +
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
