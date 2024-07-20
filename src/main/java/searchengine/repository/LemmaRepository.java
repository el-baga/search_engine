package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.entity.LemmaEntity;

import java.util.Optional;

@Repository
public interface LemmaRepository extends JpaRepository<LemmaEntity, Long> {

    Optional<LemmaEntity> findByLemmaAndSiteId(String lemma, Long siteId);

    int countAllLemmasBySiteId(Long siteId);
}
