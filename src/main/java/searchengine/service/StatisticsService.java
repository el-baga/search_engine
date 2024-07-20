package searchengine.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import searchengine.components.Site;
import searchengine.config.IndexationConfig;
import searchengine.dto.response.ApiRs;
import searchengine.components.DetailedStatisticsItem;
import searchengine.components.StatisticsData;
import searchengine.components.TotalStatistics;
import searchengine.entity.SiteEntity;
import searchengine.repository.LemmaRepository;
import searchengine.repository.PageRepository;
import searchengine.repository.SiteRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final SiteRepository siteRepository;

    private final PageRepository pageRepository;

    private final LemmaRepository lemmaRepository;

    private final IndexationConfig sites;

    private TotalStatistics totalStatistics = new TotalStatistics();
    private List<DetailedStatisticsItem> statisticsItemList = new ArrayList<>();

    public ApiRs getStatistics() {
        getTotalAndDetailedStatistics();
        ApiRs apiRs = new ApiRs();
        StatisticsData statisticsData = getStatisticsData();
        apiRs.setStatistics(statisticsData);
        apiRs.setResult(true);
        return apiRs;
    }

    private void getTotalAndDetailedStatistics() {
        List<Site> sitesList = sites.getSites();
        totalStatistics.setSites(sitesList.size());
        totalStatistics.setIndexing(true);
        for (Site site : sitesList) {
            DetailedStatisticsItem siteDetailedStatistics = new DetailedStatisticsItem(site.getUrl(), site.getName(),
                    null, LocalDateTime.now(), "-", 0, 0);
            if (siteRepository.existsByUrl(siteDetailedStatistics.getUrl())) {
                siteDetailedStatistics = setSiteDataStatistic(site);
            }
            totalStatistics.setPages(totalStatistics.getPages() + siteDetailedStatistics.getPages());
            totalStatistics.setLemmas(totalStatistics.getLemmas() + siteDetailedStatistics.getLemmas());
            statisticsItemList.add(siteDetailedStatistics);
        }
    }

    private DetailedStatisticsItem setSiteDataStatistic(Site site) {
        SiteEntity siteEntity = siteRepository.findByUrl(site.getUrl()).orElseThrow();
        String status = siteEntity.getStatus().toString();
        LocalDateTime statusTime = siteEntity.getStatusTime();
        String error = siteEntity.getLastError();
        int pages = pageRepository.countAllPagesBySiteId(siteEntity.getId());
        int lemmas = lemmaRepository.countAllLemmasBySiteId(siteEntity.getId());
        return new DetailedStatisticsItem(site.getUrl(), site.getName(), status, statusTime, error, pages, lemmas);
    }

    private StatisticsData getStatisticsData() {
        StatisticsData statisticsData = new StatisticsData();
        statisticsData.setTotal(totalStatistics);
        statisticsData.setDetailed(statisticsItemList);

        totalStatistics = new TotalStatistics();
        statisticsItemList = new ArrayList<>();
        return statisticsData;
    }
}
