package searchengine.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.components.PageRelevanceSearch;
import searchengine.entity.PageEntity;

import java.util.*;

@Component
@RequiredArgsConstructor
public class RelevanceCalculation {

    public List<PageRelevanceSearch> calculateRelevance(Map<PageEntity, Float> pages) {
        List<PageRelevanceSearch> pageRelevanceSearchList = new ArrayList<>();
        float maxRank = 1;

        for(Map.Entry<PageEntity, Float> entry : pages.entrySet()) {
           PageEntity page = entry.getKey();
           float absoluteRank = entry.getValue();

            PageRelevanceSearch pageRelevanceSearch = new PageRelevanceSearch();
            pageRelevanceSearch.setPageEntity(page);
            pageRelevanceSearch.setAbsoluteRank(absoluteRank);
            pageRelevanceSearchList.add(pageRelevanceSearch);

            if(maxRank < absoluteRank) {
                maxRank = absoluteRank;
            }
        }

        pageRelevanceSearchList.get(0).setMaxRank(maxRank);
        return pageRelevanceSearchList;
    }
}
