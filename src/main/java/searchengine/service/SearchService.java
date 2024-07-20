package searchengine.service;

import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import searchengine.components.DataPropertiesSearch;
import searchengine.components.LinkSearch;
import searchengine.components.PageRelevanceSearch;
import searchengine.components.Site;
import searchengine.config.IndexationConfig;
import searchengine.dto.response.ApiRs;
import searchengine.lemma.LemmaConverter;
import searchengine.entity.LemmaEntity;
import searchengine.entity.PageEntity;
import searchengine.entity.SiteEntity;
import searchengine.message.MessageType;
import searchengine.search.PageFinder;
import searchengine.search.RelevanceCalculation;
import searchengine.search.SnippetCreation;
import searchengine.repository.SiteRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final LemmaConverter lemmaConverter;

    private final SnippetCreation snippetCreation;

    private final RelevanceCalculation relevanceCalculation;

    private final PageFinder pageFinder;

    private final IndexationConfig sites;

    private final SiteRepository siteRepository;

    private boolean isItSearchOfAllSites = false;

    public ApiRs searchForOneSite(String query, String site) {
        ApiRs apiRs;
        SiteEntity siteEntity = siteRepository.findByUrl(site).orElseThrow();
        if (!siteEntity.getStatus().name().equals("INDEXED")) {
            apiRs = new ApiRs(false, MessageType.SITE_IS_NOT_INDEXED_ERROR.getText());
        } else if (query.isEmpty()) {
            apiRs = new ApiRs(false, MessageType.EMPTY_QUERY_ERROR.getText());
        } else {
            apiRs = getFoundResultsResponse(query, siteEntity);
        }
        return apiRs;
    }

    public ApiRs searchForAllSites(String query) {
        List<ApiRs> apiRsList = new ArrayList<>();
        List<DataPropertiesSearch> totalDataPropertySearches = new ArrayList<>();
        List<String> totalError = new ArrayList<>();
        ApiRs totalApiRs;
        int totalCount = 0;
        isItSearchOfAllSites = true;

        for (Site site : sites.getSites()) {
            ApiRs apiRs = searchForOneSite(query, site.getUrl());
            apiRsList.add(apiRs);
            totalError.add(apiRs.getError());
        }

        if (apiRsList.stream().anyMatch(s -> !s.isResult())) {
            totalApiRs = sendNegativeResponse(totalError);
        } else {
            for (ApiRs apiRs : apiRsList) {
                if (apiRs.getData().isEmpty()) {
                    continue;
                }
                totalCount += apiRs.getCount();
                totalDataPropertySearches.addAll(apiRs.getData());
            }
            totalApiRs = sendPositiveResponse(totalCount, totalDataPropertySearches);
        }
        isItSearchOfAllSites = false;
        return totalApiRs;
    }

    private ApiRs getFoundResultsResponse(String query, SiteEntity siteEntity) {
        ApiRs apiRs;
        List<DataPropertiesSearch> dataPropertiesSearchList;

        Set<LemmaEntity> lemmas = pageFinder.searchQueryWordsInDB(query, siteEntity);
        if (lemmas.isEmpty()) {
            dataPropertiesSearchList = new ArrayList<>();
            apiRs = new ApiRs(true, 0, dataPropertiesSearchList);
        } else {
            Map<PageEntity, Float> pages = pageFinder.getPagesContainingLemmaQueryWords(lemmas);
            if (pages.isEmpty()) {
                dataPropertiesSearchList = new ArrayList<>();
                apiRs = new ApiRs(true, 0, dataPropertiesSearchList);
            } else {
                dataPropertiesSearchList = saveDataIntoList(pages, lemmas);
                apiRs = getDataPropertiesListInfo(dataPropertiesSearchList);
            }
        }
        return apiRs;
    }

    private ApiRs getDataPropertiesListInfo(List<DataPropertiesSearch> dataPropertiesSearchList) {
        ApiRs apiRs;
        if (!isItSearchOfAllSites) {
            dataPropertiesSearchList = dataPropertiesSearchList.subList(0, Math.min(dataPropertiesSearchList.size(), 20));
        }
        apiRs = new ApiRs(true, dataPropertiesSearchList.size(), dataPropertiesSearchList.stream()
                .sorted(Collections.reverseOrder(Comparator.comparing(DataPropertiesSearch::getRelevance))).toList());
        return apiRs;
    }

    private ApiRs sendNegativeResponse(List<String> totalError) {
        ApiRs totalApiRs;
        if (totalError.contains(MessageType.SITE_IS_NOT_INDEXED_ERROR.getText())) {
            totalApiRs = new ApiRs(false, MessageType.SITE_IS_NOT_INDEXED_ERROR.getText());
        } else {
            totalApiRs = new ApiRs(false, MessageType.EMPTY_QUERY_ERROR.getText());
        }
        return totalApiRs;
    }

    private ApiRs sendPositiveResponse(int totalCount, List<DataPropertiesSearch> totalDataPropertySearches) {
        ApiRs totalApiRs;
        if (totalCount == 0 && totalDataPropertySearches.isEmpty()) {
            totalApiRs = new ApiRs(true, 0, totalDataPropertySearches);
        } else {
            List<DataPropertiesSearch> total = totalDataPropertySearches.subList(0, Math.min(totalDataPropertySearches.size(), 20));
            totalApiRs = new ApiRs(true, totalDataPropertySearches.size(), total.stream()
                    .sorted(Collections.reverseOrder(Comparator.comparing(DataPropertiesSearch::getRelevance))).toList());
        }
        return totalApiRs;
    }

    private List<DataPropertiesSearch> saveDataIntoList(Map<PageEntity, Float> pages, Set<LemmaEntity> lemmas) {
        List<DataPropertiesSearch> dataPropertiesSearchList = new ArrayList<>();
        List<PageRelevanceSearch> pageRelevanceSearchList = relevanceCalculation.calculateRelevance(pages);
        float maxRelevance = pageRelevanceSearchList.get(0).getMaxRank();
        for (PageRelevanceSearch pageRelevanceSearch : pageRelevanceSearchList) {
            DataPropertiesSearch dataPropertiesSearch = new DataPropertiesSearch();
            PageEntity page = pageRelevanceSearch.getPageEntity();
            LinkSearch linkSearch = editPageProperties(page.getSite().getUrl(), page.getPath(), page.getContent());
            dataPropertiesSearch.setSiteName(page.getSite().getName());
            dataPropertiesSearch.setSite(linkSearch.getSite());
            dataPropertiesSearch.setUri(linkSearch.getUri());
            dataPropertiesSearch.setTitle(linkSearch.getTitle());
            dataPropertiesSearch.setRelevance(pageRelevanceSearch.getAbsoluteRank() / maxRelevance);
            dataPropertiesSearch.setSnippet(snippetCreation.getSnippetFromPageContent(linkSearch.getContent(), lemmas));
            dataPropertiesSearchList.add(dataPropertiesSearch);
        }
        return dataPropertiesSearchList;
    }

    private LinkSearch editPageProperties(String siteURL, String uri, String pageContent) {
        LinkSearch linkSearch = new LinkSearch();
        String finalSiteURLVersion = lemmaConverter.editUrl(siteURL).toString();
        String title = pageContent.substring(pageContent.indexOf("<title>") + "<title>".length(), pageContent.indexOf("</title>"));
        String content = Jsoup.parse(pageContent).body().text();
        linkSearch.setSite(finalSiteURLVersion);
        linkSearch.setUri(uri);
        linkSearch.setTitle(title);
        linkSearch.setContent(content);
        return linkSearch;
    }
}
