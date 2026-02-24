package com.cricketbot.controller;

import com.cricketbot.entity.CricketNews;
import com.cricketbot.repository.CricketNewsRepository;
import com.cricketbot.service.CricketNewsService;
import com.cricketbot.service.MastodonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestControllerClass {

    private static final Logger log = LoggerFactory.getLogger(TestControllerClass.class);

    @Autowired
    private MastodonService mastodonService;

    @Autowired
    private CricketNewsRepository newsRepository;

    @Autowired
    private CricketNewsService newsService;

    // 1. Fetch news now
    @GetMapping("/fetch-now")
    public String fetchNow() {
        log.info("🔍 Manual fetch triggered");
        int count = newsService.fetchAndSaveNews();
        return "Fetched and saved " + count + " new articles!";
    }

    // 2. Post one news to Mastodon
    @GetMapping("/post-now")
    public String postNow() throws Exception {
        log.info("📤 Manual post triggered");
        CricketNews news = newsRepository.findFirstByPostedFalseOrderByPublishedDateAsc();
        if (news == null) {
            log.warn("⚠️ No unposted news available");
            return "No unposted news available";
        }
        mastodonService.postCricketNews(news);
        news.setPosted(true);
        newsRepository.save(news);
        log.info("✅ Manually posted: {}", news.getTitle());
        return "Posted successfully: " + news.getTitle();
    }

    // 3. Cleanup posted news
    @GetMapping("/cleanup-now")
    public String cleanupNow() {
        log.info("🧹 Manual cleanup triggered");
        int deleted = newsService.deleteAllNews();
        log.info("✅ Deleted {} posted articles", deleted);
        return "Deleted " + deleted + " posted articles!";
    }
}