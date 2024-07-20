package searchengine.components;

import lombok.*;
import searchengine.entity.PageEntity;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PageRelevanceSearch
{
    private PageEntity pageEntity;
    private float absoluteRank;
    private float maxRank;
}
