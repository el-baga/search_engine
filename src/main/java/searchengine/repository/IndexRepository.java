package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import searchengine.entity.IndexEntity;

import java.util.List;
import java.util.Optional;


public interface IndexRepository extends JpaRepository<IndexEntity, Long> {

    List<IndexEntity> findAllByLemmaId(Long lemmaId);

    List<IndexEntity> findAllByPageId(Long pageId);

    Optional<IndexEntity> findByLemmaIdAndPageId(Long lemmaId, Long pageId);

    void deleteAllByPageId(Long id);
}
