
package com.cricketbot.scheduler;

import com.cricketbot.entity.CricketNews;
import com.cricketbot.repository.CricketNewsRepository;
import com.cricketbot.service.CricketNewsService;
import com.cricketbot.service.MastodonService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class CricketBotScheduler {

    
    private CricketNewsService newsService;

    private MastodonService mastodonService;

    private CricketNewsRepository newsRepository;

    // Add this line inside the class
    private static final Logger log = LoggerFactory.getLogger(CricketBotScheduler.class);

    public void fetchNewsEvery4Hours() {
        log.info("⏰ [SCHEDULED] Fetching cricket news at {} ", LocalDateTime.now());
        try {
            int savedCount = newsService.fetchAndSaveNews();
            log.info("✅ Auto-fetch complete. Saved {} new articles.", savedCount);
        } catch (Exception e) {
            log.error("❌ Error in scheduled fetch: {}", e.getMessage());
        }
    }

    public void cleanupPostedNews() {
        log.info("🧹 [SCHEDULED] Cleaning up posted news at {}", LocalDateTime.now());
        try {
            int deletedCount = newsService.deletePostedNews();
            log.info("✅ Deleted {} posted articles.", deletedCount);
        } catch (Exception e) {
            log.error("❌ Error during cleanup: {}", e.getMessage());
        }
    }

    public void postToMastodon() {
        log.info("📤 [SCHEDULED] Attempting to post to Mastodon at {}", LocalDateTime.now());
        try {
            // Get oldest unposted news
            CricketNews news = newsRepository
                    .findFirstByPostedFalseOrderByPublishedDateAsc();

            if (news != null) {
                // Post to Mastodon
                mastodonService.postCricketNews(news);

                // Mark as posted
                news.setPosted(true);
                newsRepository.save(news);

                log.info("✅ Successfully posted: {}", news.getTitle());

            } else {
                log.warn("⚠️ No unposted news available");

            }

        } catch (Exception e) {
            log.error("❌ Error posting to Mastodon: {}", e);
        }

    }
}