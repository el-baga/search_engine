package searchengine.indexation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import searchengine.components.IndexationComponents;
import searchengine.lemma.LemmaConverter;
import searchengine.entity.IndexEntity;
import searchengine.entity.LemmaEntity;
import searchengine.entity.PageEntity;
import searchengine.entity.SiteEntity;
import searchengine.repository.IndexRepository;
import searchengine.repository.IndexationFlagRepository;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveAction;

@Slf4j
@RequiredArgsConstructor
public class LinkFinder extends RecursiveAction {

    private final transient PageRepository pageRepository;

    private final transient LemmaRepository lemmaRepository;

    private final transient IndexRepository indexRepository;

    private final transient LemmaConverter lemmaConverter;

    private final transient IndexationFlagRepository indexationFlagRepository;

    private final transient IndexationComponents indexationComponents;

    protected static final List<String> urlList = new ArrayList<>();

    @Override
    protected void compute() {
        try {
            Thread.sleep(150);
            Document document;
            Thread.sleep(150);
            document = Jsoup.connect(indexationComponents.getUrl())
                    .userAgent(indexationComponents.getIndexationConfig().getUserAgent())
                    .referrer(indexationComponents.getIndexationConfig().getReferrer())
                    .get();
            Connection.Response response = document.connection().response();
            int code = response.statusCode();
            String content = document.html();
            String path = indexationComponents.getUrl().replace(lemmaConverter.editUrl(indexationComponents.getSiteEntity().getUrl()), "");
            savePageDataIntoDB(code, content, path);
            if(indexationFlagRepository.existsByIsIndexOnePageActive(false)) parseUrl(document);
        } catch (InterruptedException | IOException e) {
            log.error(e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    private void savePageDataIntoDB(int code, String content, String path) {
        PageEntity pageEntity = new PageEntity();
        pageEntity.setSite(indexationComponents.getSiteEntity());
        pageEntity.setCode(code);
        pageEntity.setContent(content);
        pageEntity.setPath(path);
        if (indexationFlagRepository.existsByIsIndexationRunning(true)) {
            pageRepository.saveAndFlush(pageEntity);
            log.info("Parsing page-url: {}", indexationComponents.getUrl());
            Map<String, Integer> pageLemmas = lemmaConverter.convertContentToLemmas(content);
            for(Map.Entry<String, Integer> entry : pageLemmas.entrySet()) {
                String lemma = entry.getKey();
                int rank = entry.getValue();
                LemmaEntity lemmaEntity = saveLemmasToDB(lemma, pageEntity.getSite());
                saveIndexesToDB(lemmaEntity, pageEntity, rank);
            }
        }
    }

    private LemmaEntity saveLemmasToDB(String lemma, SiteEntity site) {
        LemmaEntity lemmaEntity = lemmaRepository.findByLemmaAndSiteId(lemma, site.getId()).orElse(null);

        if(lemmaEntity == null) {
            lemmaEntity = LemmaEntity.builder()
                    .lemma(lemma)
                    .frequency(1)
                    .site(site)
                    .build();
            lemmaRepository.saveAndFlush(lemmaEntity);
            log.info("'{}' - Новая лемма '{}' была добавлена", site.getName(), lemmaEntity.getLemma());
        } else {
            int updatedFrequency = lemmaEntity.getFrequency() + 1;
            lemmaEntity.setFrequency(updatedFrequency);
            lemmaRepository.saveAndFlush(lemmaEntity);
            log.info("'{}' - Частота леммы '{}' была обновлена до '{}'", site.getName(), lemmaEntity.getLemma(), updatedFrequency);
        }
        return lemmaEntity;
    }

    private void saveIndexesToDB(LemmaEntity lemmaEntity, PageEntity pageEntity, int rank) {
        IndexEntity indexEntity = IndexEntity.builder()
                .rank(rank)
                .lemma(lemmaEntity)
                .page(pageEntity)
                .build();
        indexRepository.saveAndFlush(indexEntity);
    }

    private void parseUrl(Document document) {
        Elements elements = document.select("body").select("a");
        List<LinkFinder> tasks = new ArrayList<>();
        elements.forEach(el -> {
            String link = el.attr("abs:href");
            if (checkUrlOnValidElementType(link, el.baseUri()) && !urlList.contains(link)) {
                urlList.add(link);
                LinkFinder task = new LinkFinder(
                        pageRepository,
                        lemmaRepository,
                        indexRepository,
                        lemmaConverter,
                        indexationFlagRepository,
                        IndexationComponents.builder()
                                .url(link)
                                .siteEntity(indexationComponents.getSiteEntity())
                                .indexationConfig(indexationComponents.getIndexationConfig())
                                .build()
                );
                task.fork();
                tasks.add(task);
            }
        });
        tasks.forEach(ForkJoinTask::join);
    }

    private boolean checkUrlOnValidElementType(String link, String baseUri) {
        boolean isLinkCorrect = link.startsWith(baseUri) && !link.equals(baseUri) && !link.contains("#") && !link.contains("?");
        List<String> wrongTypeList = Arrays.asList("JPG", "gif", "gz", "jar", "jpeg", "jpg", "pdf", "png", "ppt", "pptx", "svg", "svg", "tar", "zip");
        return !wrongTypeList.contains(link.substring(link.lastIndexOf(".") + 1)) && isLinkCorrect;
    }
}

