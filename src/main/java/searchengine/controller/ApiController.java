package searchengine.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import searchengine.dto.response.ApiRs;
import searchengine.service.SearchService;
import searchengine.service.StatisticsService;
import searchengine.service.IndexationService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ApiController {

    private final StatisticsService statisticsService;

    private final IndexationService indexationService;

    private final SearchService searchService;


    @GetMapping("/statistics")
    public ResponseEntity<ApiRs> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @GetMapping("/startIndexing")
    public ResponseEntity<ApiRs> startIndexing() {
        return ResponseEntity.ok(indexationService.startIndexingApiResponse());
    }

    @GetMapping("/stopIndexing")
    public ResponseEntity<ApiRs> stopIndexing() {
        return ResponseEntity.ok(indexationService.stopIndexingApiResponse());
    }

    @PostMapping("/indexPage")
    public ResponseEntity<ApiRs> indexPage(@RequestParam String url) {
        return ResponseEntity.ok(indexationService.indexPageApiResponse(url));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiRs> search(
            @RequestParam(name = "query", required = false, defaultValue = "") String query,
            @RequestParam(name = "site", required = false, defaultValue = "") String site,
            int offset, int limit) {
        return site.isEmpty() ? ResponseEntity.ok(searchService.searchForAllSites(query))
                : ResponseEntity.ok(searchService.searchForOneSite(query, site));
    }
}
