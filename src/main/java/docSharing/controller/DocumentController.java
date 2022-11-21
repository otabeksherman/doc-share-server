package docSharing.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Controller
public class DocumentController {
    private UpdateMessage currentState = new UpdateMessage();
    Runnable updateDocumentBody = new Runnable() {
        public void run() {
            System.out.println(currentState);
        }
    };
    public DocumentController(){
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(updateDocumentBody, 0, 5, TimeUnit.SECONDS);
    }
    @MessageMapping("/join")
    public void sendPlainMessage(JoinMessage message) {
        System.out.println(message.user + " joined");
    }

    @RequestMapping("/document/update")
    public ResponseEntity<String> sendPlainMessage(@RequestBody UpdateMessage message){
        currentState = message;
        return new ResponseEntity<>("message updated", HttpStatus.OK);
    }

    static class UpdateMessage {
        private int userId;
        private String content;

        private int documentId;

        public UpdateMessage() {
        }

        public int getUserId() {
            return userId;
        }

        public String getContent() {
            return content;
        }

        public int getDocumentId() {
            return documentId;
        }

        public void setUserId(int userId) {
            this.userId = userId;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public void setDocumentId(int documentId) {
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
