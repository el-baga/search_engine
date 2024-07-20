package searchengine.components;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import searchengine.config.IndexationConfig;
import searchengine.entity.SiteEntity;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class IndexationComponents {
    private String url;
    private SiteEntity siteEntity;
    private IndexationConfig indexationConfig;
}
