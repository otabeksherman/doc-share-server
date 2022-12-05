package docSharing.repository;

import docSharing.Entities.ChangeLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Change Log Repository for keeping changes performed in each document.
 */
@Repository
public interface ChangeLogRepository extends JpaRepository<ChangeLog, Long> {
    List<ChangeLog> findByDocumentId(Long id);
}
