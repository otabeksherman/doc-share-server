package docSharing.repository;

import docSharing.Entities.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface FolderRepository  extends JpaRepository<Folder, Long> {
    Optional<Folder> findByOwnerIdAndParentFolderIsNull(Long ownerId);

    Set<Folder> findByParentFolderIdAndOwnerId(Long parentId, Long userId);
}
