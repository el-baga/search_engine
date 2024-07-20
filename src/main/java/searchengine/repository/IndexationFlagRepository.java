package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.entity.IndexationFlagEntity;

import java.util.Optional;

@Repository
public interface IndexationFlagRepository extends JpaRepository<IndexationFlagEntity, Long> {

    Optional<IndexationFlagEntity> findByIsIndexationRunning(boolean isIndexationRunning);

    boolean existsByIsIndexationRunning(boolean isIndexationRunning);

    Optional<IndexationFlagEntity> findByIsIndexOnePageActive(boolean isIndexOnePageActive);

    boolean existsByIsIndexOnePageActive(boolean isIndexOnePageActive);
}
