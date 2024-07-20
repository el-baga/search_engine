package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.entity.SiteEntity;
import searchengine.entity.StatusType;

import java.util.List;
import java.util.Optional;

@Repository
public interface SiteRepository extends JpaRepository<SiteEntity, Long> {

    List<SiteEntity> findAllByStatus(StatusType statusType);

    Optional<SiteEntity> findByUrl(String url);

    boolean existsByUrl(String url);
}
