package docSharing.service;

import docSharing.Entities.*;
import docSharing.repository.ChangeLogRepository;
import docSharing.repository.DocumentRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.context.event.EventListener;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;

@Service
public class ChangeLogService {

    private static final Logger LOGGER = LogManager.getLogger(ChangeLogService.class);

    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private ChangeLogRepository changeLogRepository;

    private Map<Long, Queue<UpdateResponseMessage>> documentWithLogs = new HashMap<>();

    private Map<String, List<UpdateMessage>> changeLogWithUpdates = new HashMap<>();
    private Map<String, ChangeLog> currentLogMessageSequenceInProcess = new HashMap<>();
    private List<ChangeLog> changeLogList = Collections.synchronizedList(new LinkedList<>());

    Thread runnerThread = new Thread(this::runner);

    ScheduledExecutorService saveExecutor = Executors.newScheduledThreadPool(1);

    public ChangeLogService() {
        saveExecutor.scheduleAtFixedRate(this::changeLogTimeOutRunner, 60, 3, TimeUnit.SECONDS);
    }

    /**
     * Function for filtering update messages by document id and inserting update
     * messages to the queue. Invokes runner thread for log processing.
     * @param message
     */
    public void addLog(UpdateResponseMessage message) {
        LOGGER.debug("Adding message to queue");
        documentWithLogs.computeIfAbsent(message.getDocumentId(), k -> new ConcurrentLinkedQueue<>()).add(message);
        if (!runnerThread.isAlive()) {
            LOGGER.info(String.format("Executing runner of document changer for document:%d", 
                    message.getDocumentId()));
            runnerThread = new Thread(this::runner);
            runnerThread.start();
        }
    }

    /**
     * Runner function for creating change logs for each document.
     */
    private void runner() {
        while (documentWithLogs.keySet().size() != 0) {
            Set<Long> docIdsSet = documentWithLogs.keySet();
            Long[] docIdsArr = docIdsSet.toArray(new Long[docIdsSet.size()]);
            int index = ThreadLocalRandom.current().nextInt(docIdsSet.size());
            Long docId = docIdsArr[index];
            Queue<UpdateResponseMessage> updateMessages = documentWithLogs.get(docId);
            LOGGER.info("Building logs for document:%d");
            while (!updateMessages.isEmpty()) {
                UpdateResponseMessage message = updateMessages.poll();
                StringBuffer sb = new StringBuffer(message.getContent());
                if (currentLogMessageSequenceInProcess.containsKey(message.getEmail())) {
                    ChangeLog changeLog = currentLogMessageSequenceInProcess
                            .get(message.getEmail());
                    UpdateType logType = changeLog.getUpdateType();
                    UpdateType messageType = message.getType();
                    if (changeLog.getEndPosition() == message.getPosition()
                            && (logType == messageType
                            || logType == UpdateType.APPEND && messageType == UpdateType.APPEND_RANGE
                            || logType == UpdateType.APPEND_RANGE && messageType == UpdateType.APPEND
                            || logType == UpdateType.DELETE && messageType == UpdateType.DELETE_RANGE
                            || logType == UpdateType.DELETE_RANGE && messageType == UpdateType.DELETE)) {
                        if (messageType == UpdateType.APPEND) {
                            changeLog.appendText(message.getContent());
                            changeLog.forwardChangeLogEndIndex();
                            changeLog.setLastModified(LocalDateTime.now());
                            int logIndex = changeLogList.indexOf(changeLog);
                            for (int i = logIndex + 1; i < changeLogList.size(); i++) {
                                ChangeLog nextLog = changeLogList.get(i);
                                nextLog.forwardChangeLogIndexes();
                            }
                        }
                        if (messageType == UpdateType.APPEND_RANGE) {
                            changeLog.appendText(message.getContent());
                            changeLog.forwardChangeLogEndIndex(message.getContent().length());
                            changeLog.setLastModified(LocalDateTime.now());
                            int logIndex = changeLogList.indexOf(changeLog);
                            for (int i = logIndex + 1; i < changeLogList.size(); i++) {
                                ChangeLog nextLog = changeLogList.get(i);
                                nextLog.forwardChangeLogIndexes(message.getContent().length());
                            }
                        }
                        if (messageType == UpdateType.DELETE) {
                            changeLog.appendTextToHead(message.getContent());
                            changeLog.backChangeLogStartIndex();
                            changeLog.setLastModified(LocalDateTime.now());
                            int logIndex = changeLogList.indexOf(changeLog);
                            for (int i = logIndex + 1; i < changeLogList.size(); i++) {
                                ChangeLog nextLog = changeLogList.get(i);
                                nextLog.backChangeLogIndexes();
                            }
                        }
                        if (messageType == UpdateType.DELETE_RANGE) {
                            changeLog.appendTextToHead(message.getContent());
                            changeLog.backChangeLogStartIndex(message.getContent().length());
                            changeLog.setLastModified(LocalDateTime.now());
                            int logIndex = changeLogList.indexOf(changeLog);
                            for (int i = logIndex + 1; i < changeLogList.size(); i++) {
                                ChangeLog nextLog = changeLogList.get(i);
                                nextLog.backChangeLogIndexes(message.getContent().length());
                            }
                        }

                    } else {
                        changeLogRepository.save(changeLog);
                        changeLogList.remove(changeLog);
                        changeLog = new ChangeLog(message.getDocumentId(), message.getPosition(),
                                message.getEmail(), message.getContent(), message.getType());
                        currentLogMessageSequenceInProcess.put(message.getEmail(), changeLog);
                        putLogInRightPlace(changeLog);
                    }
                } else {
                    ChangeLog changeLog = new ChangeLog(message.getDocumentId(), message.getPosition(),
                            message.getEmail(), message.getContent(), message.getType() );
                    currentLogMessageSequenceInProcess.put(message.getEmail(), changeLog);
                    putLogInRightPlace(changeLog);
                }
                if (updateMessages.isEmpty()) {
                    LOGGER.debug("Waiting for incoming messages...");
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }



                // ########## Working version - Start ###########
//                if (updateMessages.isEmpty()) {
//                    LOGGER.debug("Waiting for incoming messages 2...");
//                    try {
//                        Thread.sleep(10000);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
//                while (!updateMessages.isEmpty() && updateMessages.peek().getPosition()
//                        == message.getPosition() + 1 && updateMessages.peek().getEmail()
//                        .equals(message.getEmail()) && updateMessages.peek().getType()
//                        == message.getType()) {
//                    message = updateMessages.poll();
//                    sb.append(message.getContent());
//                    changeLog.forwardChangeLogEndIndex();
//                    LOGGER.debug(sb.toString());
//                    if (updateMessages.isEmpty()) {
//                        LOGGER.debug("Waiting for incoming messages 2...");
//                        try {
//                            Thread.sleep(10000);
//                        } catch (InterruptedException e) {
//                            throw new RuntimeException(e);
//                        }
//                    }
//                    changeLog.setBody(sb.toString());
//                    changeLogRepository.save(changeLog);
//                }
                // ########## Working version -End ##########

                    // Check if sequence of messages from specific email is already in process
//                if (currentLogMessageSequenceInProcess.containsKey(message.getUser())) {
//                    ChangeLog changeLog = currentLogMessageSequenceInProcess.get(message.getUser());
//                    List<UpdateMessage> logMessages = changeLogWithUpdates.get(message.getUser());
//                    UpdateMessage lastMessageInLog = logMessages.get(logMessages.size() - 1);
//                    while (!updateMessages.isEmpty() || message.getPosition()
//                            == lastMessageInLog.getPosition() + 1) {
//                        logMessages.add(message);
//                    }
//                    StringBuffer sb = new StringBuffer(changeLog.getBody());
//                    for (UpdateMessage updateMessage : logMessages) {
//                        sb.append(updateMessage.getContent());
//                    }
//                    Optional<Document> document = documentRepository.findById(changeLog.getDocumentId());
//                    List<ChangeLog> changeLogList = document.get().getChangeLogList();
//                    changeLogList.add(changeLog);
//                    documentRepository.save(document.get());
//                    currentLogMessageSequenceInProcess.remove(message.getUser());
//                } else {
//                    ChangeLog changeLog = new ChangeLog(message.getDocumentId(), message.getPosition(),
//                            message.getUser(), message.getContent());
//                    changeLogWithUpdates.put(changeLog.getEmail(), List.of(message));
//                    currentLogMessageSequenceInProcess.put(message.getUser(), changeLog);
//                }
//                Optional<Document> optDocument= documentRepository.findById(changeLog.getDocumentId());
//                if (optDocument.isPresent()) {
//                    Document document = optDocument.get();
//                    document.getBody();
//                    document.setBody(sb.toString());
//                    documentRepository.save(document);
//                    changeLogRepository.save(changeLog);
            }
            if (updateMessages.isEmpty()) {
                documentWithLogs.remove(docId);
            }
        }
    }

    private void changeLogTimeOutRunner() {
        for (ChangeLog log : changeLogList) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime lastModified = log.getLastModified();
            long minutes = ChronoUnit.MINUTES.between(lastModified, now);
            if (minutes > 0) {
                changeLogRepository.save(log);
                changeLogList.remove(log);
                currentLogMessageSequenceInProcess.remove(log.getEmail());
            }
        }
    }

    /**
     * Function for tracking Logs by it position
     * @param log
     */
    private void putLogInRightPlace(ChangeLog log) {
        if (changeLogList.isEmpty()) {
            changeLogList.add(log);
        } else {
            ListIterator<ChangeLog> changeLogListIterator = changeLogList.listIterator();
            while (changeLogListIterator.hasNext()) {
                ChangeLog next = changeLogListIterator.next();
                if (log.getStartPosition() <= next.getStartPosition()) {
                    changeLogListIterator.add(log);
                }
            }
            while (changeLogListIterator.hasNext()) {
                changeLogListIterator.next().forwardChangeLogIndexes();
            }
        }
    }

    public List<ChangeLog> getChangeLogs(Long docId) {
        List<ChangeLog> logs = changeLogRepository.findByDocumentId(docId);
        if (!logs.isEmpty()) {
            return logs;
        } else {
            throw new IllegalArgumentException();
        }
    }

//    @EventListener(SessionDisconnectEvent.class)
//    public void handleWebsocketDisconnectListener(SessionDisconnectEvent event) {
//        LOGGER.info("session closed : " + now());
//        LOGGER.info(event.getMessage().getHeaders().get("simpSessionId").toString());
//    }
}
