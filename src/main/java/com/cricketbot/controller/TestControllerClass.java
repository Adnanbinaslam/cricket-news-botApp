package com.cricketbot.controller;

import com.cricketbot.entity.CricketNews;
import com.cricketbot.repository.CricketNewsRepository;
import com.cricketbot.service.CricketNewsService;
import com.cricketbot.service.MastodonService;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestControllerClass {

    private static final Logger log = LoggerFactory.getLogger(TestControllerClass.class);
    
    private final MastodonService mastodonService;
    private final CricketNewsRepository newsRepository;
    private final CricketNewsService newsService;

    // 🎯 ONE-LINE DTO: No getters, setters, or mappers required!
    public record ApiResponse(boolean success, String message, Object data) {}

    @GetMapping("/fetch-now")
    public ResponseEntity<ApiResponse> fetchNow() {
        log.info("🔍 Manual fetch triggered");
        int count = newsService.fetchAndSaveNews();
        
        // Pass your data directly into the record constructor
        ApiResponse response = new ApiResponse(true, "Fetched and saved " + count + " new articles!", count);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/post-now")
    public ResponseEntity<ApiResponse> postNow() throws Exception {
        log.info("📤 Manual post triggered");
        CricketNews news = newsRepository.findFirstByPostedFalseOrderByPublishedDateAsc();
        
        if (news == null) {
            log.warn("⚠️ No unposted news available");
            return ResponseEntity.ok(new ApiResponse(false, "No unposted news available", null));
        }
        
        mastodonService.postCricketNews(news);
        news.setPosted(true);
        newsRepository.save(news);
        log.info("✅ Manually posted: {}", news.getTitle());
        
        return ResponseEntity.ok(new ApiResponse(true, "Posted successfully: " + news.getTitle(), news.getTitle()));
    }

    @GetMapping("/cleanup-now")
    public ResponseEntity<ApiResponse> cleanupNow() {
        log.info("🧹 Manual cleanup triggered");
        int deleted = newsService.deleteAllNews();
        log.info("✅ Deleted {} posted articles", deleted);
        
        return ResponseEntity.ok(new ApiResponse(true, "Deleted " + deleted + " posted articles!", deleted));
    }
}
