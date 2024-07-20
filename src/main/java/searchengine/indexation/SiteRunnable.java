package searchengine.indexation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import searchengine.config.IndexationConfig;
import searchengine.components.IndexationComponents;
import searchengine.lemma.LemmaConverter;
import searchengine.entity.IndexationFlagEntity;
import searchengine.entity.SiteEntity;
import searchengine.entity.StatusType;
import searchengine.repository.*;

import java.time.LocalDateTime;
import java.util.concurrent.ForkJoinPool;

@Slf4j
@RequiredArgsConstructor
public class SiteRunnable implements Runnable {
    private final String siteUrl;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final LemmaRepository lemmaRepository;
    private final IndexRepository indexRepository;
    private final LemmaConverter lemmaConverter;
    private final IndexationConfig indexationConfig;
    private final IndexationFlagRepository indexationFlagRepository;

    @Override
    public void run() {
        SiteEntity site = siteRepository.findByUrl(siteUrl).orElse(null);
        String url = lemmaConverter.editUrl(siteUrl).toString();
        if(site == null) {
            StringBuilder builder = new StringBuilder(url);
            int index = url.indexOf("://") + 3;
            if(!url.contains("www.")) {
                builder.insert(index, "www.");
            }
            int index2 = builder.indexOf("/", index);
            String extractedSiteUrlFromUrl = builder.substring(0, index2);
            site = siteRepository.findByUrl(extractedSiteUrlFromUrl).orElseThrow();
        } else {
            url = url.endsWith("/") ? url : url.concat("/");
        }
        ForkJoinPool fjp = new ForkJoinPool();
        fjp.invoke(new LinkFinder(
                pageRepository,
                lemmaRepository,
                indexRepository,
                lemmaConverter,
                indexationFlagRepository,
                IndexationComponents.builder()
                        .indexationConfig(indexationConfig)
                        .siteEntity(site)
                        .url(url)
                        .build()));
        if (indexationFlagRepository.existsByIsIndexationRunning(true) && indexationFlagRepository.existsByIsIndexOnePageActive(false)) {
            setIndexedStatus(site);
            boolean isIndexationFinished = siteRepository.findAllByStatus(StatusType.INDEXED).size() == indexationConfig.getSites().size();
            if(isIndexationFinished) {
                setIndexationRunningFalse();
                LinkFinder.urlList.clear();
            }
        } else {
            setIndexationRunningFalse();
            log.info("Страница с url - '{}' была успешно добавлена в базу данных", url);
            fjp.shutdownNow();
        }
    }

    private void setIndexationRunningFalse() {
        IndexationFlagEntity indexationFlag = indexationFlagRepository.findByIsIndexationRunning(true).orElseThrow();
        indexationFlag.setIndexationRunning(false);
        indexationFlag.setIndexOnePageActive(false);
        indexationFlagRepository.saveAndFlush(indexationFlag);
    }

    private void setIndexedStatus(SiteEntity site) {
        var siteEntity = siteRepository.findById(site.getId()).orElseThrow();
        siteEntity.setStatus(StatusType.INDEXED);
        siteEntity.setStatusTime(LocalDateTime.now());
        siteRepository.saveAndFlush(siteEntity);
        log.info("</Сайт {} сохранен/>", site.getName());
    }
}