package searchengine.search;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import searchengine.lemma.LemmaConverter;
import searchengine.entity.IndexEntity;
import searchengine.entity.LemmaEntity;
import searchengine.entity.PageEntity;
import searchengine.entity.SiteEntity;
import searchengine.repository.IndexRepository;
import searchengine.repository.LemmaRepository;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PageFinder {

    private final LemmaConverter lemmaConverter;

    private final LemmaRepository lemmaRepository;

    private final IndexRepository indexRepository;

    public Set<LemmaEntity> searchQueryWordsInDB(String query, SiteEntity siteEntity) {
        String[] words = lemmaConverter.splitTextToWords(query);
        Set<LemmaEntity> lemmaSet = new HashSet<>();

        for (String word : words) {
            List<String> wordBaseForms = lemmaConverter.returnWordIntoBaseForm(word);
            if (wordBaseForms.isEmpty()) {
                continue;
            }

            String resultWordForm = wordBaseForms.get(wordBaseForms.size() - 1);
            LemmaEntity lemmaEntity = lemmaRepository.findByLemmaAndSiteId(resultWordForm, siteEntity.getId()).orElse(null);
            if (lemmaEntity == null) {
                return new HashSet<>();
            } else {
                lemmaSet.add(lemmaEntity);
            }
        }
        return lemmaSet.isEmpty() ? lemmaSet : lemmaSet.stream()
                .sorted(Comparator.comparing(LemmaEntity::getFrequency))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public Map<PageEntity, Float> getPagesContainingLemmaQueryWords(Set<LemmaEntity> lemmas) {
        HashMap<PageEntity, Float> pages = new HashMap<>();
        Long lemmaId = lemmas.stream().findFirst().orElseThrow().getId();
        List<IndexEntity> indexes = indexRepository.findAllByLemmaId(lemmaId);

        List<LemmaEntity> lemmasWithoutFirst = lemmas.stream().skip(1).toList();
        indexes.forEach(index -> {
            PageEntity page = index.getPage();
            boolean containsLemmas = true;
            float pageAbsoluteRank = index.getRank();

            for (LemmaEntity lemma : lemmasWithoutFirst) {
                IndexEntity indexEntity = indexRepository.findByLemmaIdAndPageId(page.getId(), lemma.getId()).orElse(null);
                if(indexEntity == null) {
                    containsLemmas = false;
                    break;
                } else {
                    pageAbsoluteRank += indexEntity.getRank();
                }
            }

            if(containsLemmas) {
                pages.put(page, pageAbsoluteRank);
            }
        });

        return pages;
    }
}
