package searchengine.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.validator.routines.UrlValidator;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Service;
import searchengine.config.*;
import searchengine.components.Site;
import searchengine.entity.*;
import searchengine.indexation.SiteRunnable;
import searchengine.dto.response.ApiRs;
import searchengine.lemma.LemmaConverter;
import searchengine.message.MessageType;
import searchengine.repository.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class IndexationService {

    private final LemmaConverter lemmaConverter;

    private final IndexationConfig indexationConfig;

    private final SiteRepository siteRepository;

    private final PageRepository pageRepository;

    private final LemmaRepository lemmaRepository;

    private final IndexRepository indexRepository;

    private final IndexationFlagRepository indexationFlagRepository;

    private boolean isStartIndexingMethodActive = false;

    private ExecutorService executorService;

    public ApiRs startIndexingApiResponse() {
        isStartIndexingMethodActive = true;
        if (isIndexationRunning()) {
            return new ApiRs(false, MessageType.INDEXATION_IS_ALREADY_RUNNING_ERROR.getText());
        } else {
            List<Site> siteList = indexationConfig.getSites();
            executorService = Executors.newCachedThreadPool();
            cleanAllDataBeforeIndexing();
            IndexationFlagEntity indexationFlag = indexationFlagRepository.findByIsIndexationRunning(false).orElse(new IndexationFlagEntity());
            indexationFlag.setIndexationRunning(true);
            indexationFlag.setIndexOnePageActive(false);
            indexationFlagRepository.saveAndFlush(indexationFlag);
            for (Site site : siteList) {
                SiteEntity siteEntity = setIndexingStatus(site);
                parseSite(siteEntity);
            }
            executorService.shutdown();
        }
        return new ApiRs(true);
    }

    public ApiRs stopIndexingApiResponse() {
        isStartIndexingMethodActive = false;
        if (!isIndexationRunning()) {
            return new ApiRs(true, MessageType.INDEXATION_IS_NOT_RUNNING.getText());
        } else {
            List<SiteEntity> siteList = siteRepository.findAllByStatus(StatusType.INDEXING);
            IndexationFlagEntity indexationFlag = indexationFlagRepository.findByIsIndexationRunning(true).orElseThrow();
            indexationFlag.setIndexationRunning(false);
            indexationFlagRepository.saveAndFlush(indexationFlag);
            for (SiteEntity site : siteList) {
                executorService.shutdownNow();
                var siteEntity = siteRepository.findById(site.getId()).orElseThrow();
                siteEntity.setStatus(StatusType.FAILED);
                siteEntity.setStatusTime(LocalDateTime.now());
                siteEntity.setLastError(MessageType.INDEXATION_IS_STOPPED_BY_USER.getText());
                siteRepository.saveAndFlush(siteEntity);
                log.info("</Индексация сайта {} была остановлена/>", site.getName());
            }
            return new ApiRs(true);
        }
    }

    public ApiRs indexPageApiResponse(String url) {
        if (url.isEmpty()) {
            return new ApiRs(false, MessageType.URL_EMPTY_ERROR.getText());
        } else {
            if (isUrlStartingWithSitePath(url)) {
                IndexationFlagEntity indexationFlag = indexationFlagRepository.findByIsIndexOnePageActive(false).orElseThrow();
                indexationFlag.setIndexationRunning(true);
                indexationFlag.setIndexOnePageActive(true);
                indexationFlagRepository.saveAndFlush(indexationFlag);
                parsePage(url);
                return new ApiRs(true);
            } else {
                return new ApiRs(false, MessageType.INVALID_URL_ERROR.getText());
            }
        }
    }

    private boolean isIndexationRunning() {
        if (indexationFlagRepository.existsByIsIndexationRunning(true)) {
            if (isStartIndexingMethodActive) {
                List<SiteEntity> indexedSiteList = siteRepository.findAllByStatus(StatusType.INDEXING);
                return indexedSiteList.size() == indexationConfig.getSites().size();
            } else {
                return true;
            }
        } else {
            return false;
        }
    }

    private void parseSite(SiteEntity siteEntity) {
        if (isUrlValid(siteEntity.getUrl())) {
            executorService.submit(
                    new SiteRunnable(
                            siteEntity.getUrl(),
                            siteRepository, pageRepository,
                            lemmaRepository, indexRepository,
                            lemmaConverter, indexationConfig, indexationFlagRepository)
            );
        } else {
            setFailedStatus(siteEntity);
        }
    }

    private void parsePage(String url) {
        cleanPageDataBeforeIndexing(url);
        executorService = Executors.newCachedThreadPool();
        executorService.submit(new SiteRunnable(url,
                siteRepository, pageRepository,
                lemmaRepository, indexRepository,
                lemmaConverter, indexationConfig, indexationFlagRepository));
        executorService.shutdown();
    }

    private void cleanPageDataBeforeIndexing(String url) {
        String path = convertUrlToPath(url);
        PageEntity page = pageRepository.findByPath(path).orElse(null);
        if (page != null) {
            List<IndexEntity> indexIds = indexRepository.findAllByPageId(page.getId());
            indexRepository.deleteAll(indexIds);
            indexRepository.deleteAllByPageId(page.getId());
            String content = Jsoup.parse(page.getContent()).text();
            Map<String, Integer> pageLemmas = lemmaConverter.convertContentToLemmas(content);
            deletePageLemmas(pageLemmas, page.getSite().getId());
            pageRepository.delete(page);
        }
    }

    private String convertUrlToPath(String url) {
        String pageUrl = String.valueOf(lemmaConverter.editUrl(url));
        String siteUrl = "";
        List<Site> sites = indexationConfig.getSites();
        for(Site site : sites) {
            siteUrl = String.valueOf(lemmaConverter.editUrl(site.getUrl()));
            if(pageUrl.startsWith(siteUrl)) {
                break;
            }
        }
        return pageUrl.replace(siteUrl, "");
    }

    private void deletePageLemmas(Map<String, Integer> pageLemmas, Long siteId) {
        List<Long> lemmaIds = new ArrayList<>();
        for(Map.Entry<String, Integer> entry : pageLemmas.entrySet()) {
            String pageLemma = entry.getKey();
            LemmaEntity lemma = lemmaRepository.findByLemmaAndSiteId(pageLemma, siteId).orElseThrow();

            int frequency = lemma.getFrequency() - 1;
            if(frequency == 0) {
                lemmaIds.add(lemma.getId());
            } else {
                lemma.setFrequency(frequency);
                lemmaRepository.save(lemma);
            }
        }
        lemmaRepository.deleteAllById(lemmaIds);
    }

    private void cleanAllDataBeforeIndexing() {
        indexRepository.deleteAllInBatch();
        lemmaRepository.deleteAllInBatch();
        pageRepository.deleteAllInBatch();
        siteRepository.deleteAllInBatch();
        indexationFlagRepository.deleteAllInBatch();
    }

    private SiteEntity setIndexingStatus(Site site) {
        SiteEntity siteEntity = new SiteEntity();
        siteEntity.setName(site.getName());
        siteEntity.setUrl(site.getUrl());
        siteEntity.setStatus(StatusType.INDEXING);
        siteEntity.setLastError("-");
        siteEntity.setStatusTime(LocalDateTime.now());
        siteRepository.save(siteEntity);
        return siteEntity;
    }

    private void setFailedStatus(SiteEntity siteEntity) {
        siteEntity.setStatusTime(LocalDateTime.now());
        siteEntity.setLastError(MessageType.SITE_IS_NOT_AVAILABLE_ERROR.getText());
        siteEntity.setStatus(StatusType.FAILED);
        siteRepository.saveAndFlush(siteEntity);
    }

    private boolean isUrlValid(String url) {
        UrlValidator validator = new UrlValidator();
        return validator.isValid(url);
    }

    private boolean isUrlStartingWithSitePath(String url) {
        return indexationConfig.getSites().stream().anyMatch(site -> {
            String editedSiteUrl = lemmaConverter.editUrl(site.getUrl()).toString();
            String editedUrl = lemmaConverter.editUrl(url).toString();
            return editedUrl.startsWith(editedSiteUrl);
        });
    }
}
