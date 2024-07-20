package searchengine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import searchengine.entity.PageEntity;
import java.util.Optional;

@Repository
public interface PageRepository extends JpaRepository<PageEntity, Long> {

    Optional<PageEntity> findByPath(String path);

    int countAllPagesBySiteId(Long siteId);
}
