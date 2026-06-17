
package com.cricketbot.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cricketbot.entity.CricketNews;
import com.cricketbot.service.CricketNewsService;


@RestController
@RequestMapping("/api/news")
public class CricketNewsController {
    

    private final CricketNewsService newsService;

    public CricketNewsController(CricketNewsService newsService) {
        this.newsService = newsService;
    }
    
    /**
     * Manually trigger RSS feed fetch
     * GET /api/news/fetch
     */
    @GetMapping("/fetch")
    public ResponseEntity<Map<String, Object>> fetchNews() {
        int savedCount = newsService.fetchAndSaveNews();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Fetched and saved " + savedCount + " new articles");
        response.put("savedCount", savedCount);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get all news from database
     * GET /api/news/all
     */
    @GetMapping("/all")
    public ResponseEntity<List<CricketNews>> getAllNews() {
        List<CricketNews> newsList = newsService.getAllNews();
        return ResponseEntity.ok(newsList);
    }
    
    /**
     * Get unposted news
     * GET /api/news/unposted
     */
    @GetMapping("/unposted")
    public ResponseEntity<List<CricketNews>> getUnpostedNews() {
        List<CricketNews> newsList = newsService.getUnpostedNews();
        return ResponseEntity.ok(newsList);
    }

    @GetMapping("/post")
    public ResponseEntity<Map<String, Object>> postNews() throws Exception {
        log.info("📤 Post triggered via Kestra");
        Map<String, Object> response = new HashMap<>();
        
        CricketNews news = newsRepository.findFirstByPostedFalseOrderByPublishedDateAsc();
        if (news == null) {
            log.warn("⚠️ No unposted news available");
            response.put("success", false);
            response.put("message", "No unposted news available");
            return ResponseEntity.ok(response);
        }
        
        mastodonService.postCricketNews(news);
        news.setPosted(true);
        newsRepository.save(news);
        log.info("✅ Posted successfully: {}", news.getTitle());
        
        response.put("success", true);
        response.put("message", "Posted successfully: " + news.getTitle());
        return ResponseEntity.ok(response);
    }
    
    /**
     * Get total count of news
     * GET /api/news/count
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> getCount() {
        long count = newsService.getNewsCount();
        
        Map<String, Object> response = new HashMap<>();
        response.put("totalNews", count);
        
        return ResponseEntity.ok(response);
    }
}