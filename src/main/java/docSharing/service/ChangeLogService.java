package docSharing.service;

import docSharing.Entities.ChangeLog;
import docSharing.Entities.Document;
import docSharing.Entities.UpdateMessage;
import docSharing.Entities.UpdateType;
import docSharing.repository.ChangeLogRepository;
import docSharing.repository.DocumentRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ChangeLogService {

    private static final Logger LOGGER = LogManager.getLogger(ChangeLogService.class);

    @Autowired
    private DocumentRepository documentRepository;
    @Autowired
    private ChangeLogRepository changeLogRepository;

    private Map<Long, Queue<UpdateMessage>> documentWithLogs = new HashMap<>();
    private Map<String, List<UpdateMessage>> changeLogWithUpdates = new HashMap<>();
    private Map<String, ChangeLog> currentLogMessageSequenceInProcess = new HashMap<>();
    private List<ChangeLog> changeLogList = Collections.synchronizedList(new LinkedList<>());

    Thread runnerThread = new Thread(this::runner);

    ScheduledExecutorService saveExecutor = Executors.newScheduledThreadPool(1);

    public ChangeLogService() {
    }

    /**
     * Function for filtering update messages by document id and inserting update
     * messages to the queue. Invokes runner thread for log processing.
     * @param message
     */
    public void addLog(UpdateMessage message) {
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
            Queue<UpdateMessage> updateMessages = documentWithLogs.get(docId);
            LOGGER.info("Building logs for document:%d");
            while (!updateMessages.isEmpty()) {
                UpdateMessage message = updateMessages.poll();
                StringBuffer sb = new StringBuffer(message.getContent());
                ChangeLog changeLog = new ChangeLog(message.getDocumentId(), message.getPosition(),
                            message.getUser(), message.getContent());
//                if (currentLogMessageSequenceInProcess.containsKey(message.getUser())) {
//                    ChangeLog changeLog = currentLogMessageSequenceInProcess
//                            .get(message.getUser());
//                    if (changeLog.getEndPosition() == message.getPosition()) {
//                        changeLog.appendText(message.getContent());
//                        changeLog.forwardChangeLogEndIndex();
//                        changeLog.setLastModified(LocalDateTime.now());
//                        int logIndex = changeLogList.indexOf(changeLog);
//                        for (int i = logIndex + 1; i < changeLogList.size(); i++) {
//                            ChangeLog nextLog = changeLogList.get(i);
//                            nextLog.forwardChangeLogIndexes();
//                        }
//                    } else {
//                        changeLogRepository.save(changeLog);
//                        changeLog = new ChangeLog(message.getDocumentId(), message.getPosition(),
//                                message.getUser(), message.getContent());
//                        currentLogMessageSequenceInProcess.put(message.getUser(), changeLog);
//                        putLogInRightPlace(changeLog);
//                    }
//                } else {
//                    ChangeLog changeLog = new ChangeLog(message.getDocumentId(), message.getPosition(),
//                            message.getUser(), message.getContent());
//                    currentLogMessageSequenceInProcess.put(message.getUser(), changeLog);
//                    putLogInRightPlace(changeLog);
//                }
//                if (updateMessages.isEmpty()) {
//                    LOGGER.debug("Waiting for incoming messages 1...");
//                    try {
//                        Thread.sleep(5000);
//                    } catch (InterruptedException e) {
//                        throw new RuntimeException(e);
//                    }
//                }
                if (updateMessages.isEmpty()) {
                    LOGGER.debug("Waiting for incoming messages 2...");
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                while (!updateMessages.isEmpty() && updateMessages.peek().getPosition()
                        == message.getPosition() + 1 && updateMessages.peek().getUser()
                        .equals(message.getUser())) {
                    message = updateMessages.poll();
                    sb.append(message.getContent());
                    changeLog.forwardChangeLogEndIndex();
                    LOGGER.debug(sb.toString());
                    if (updateMessages.isEmpty()) {
                        LOGGER.debug("Waiting for incoming messages 2...");
                        try {
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }

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
                changeLog.setBody(sb.toString());
                changeLogRepository.save(changeLog);
//                Optional<Document> optDocument= documentRepository.findById(changeLog.getDocumentId());
//                if (optDocument.isPresent()) {
//                    Document document = optDocument.get();
//                    document.getBody();
//                    document.setBody(sb.toString());
//                    documentRepository.save(document);
//                    changeLogRepository.save(changeLog);
                }
            }
            if (updateMessages.isEmpty()) {
                documentWithLogs.remove(docId);
            }
        }
    }

    /**
     * Function for tracking Logs by it position
     * @param log
     */
    private void putLogInRightPlace(ChangeLog log) {
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

    public List<ChangeLog> getChangeLogs(Long docId) {
        List<ChangeLog> logs = changeLogRepository.findByDocumentId(docId);
        if (!logs.isEmpty()) {
            return logs;
        } else {
            throw new IllegalArgumentException();
        }
    }
}
