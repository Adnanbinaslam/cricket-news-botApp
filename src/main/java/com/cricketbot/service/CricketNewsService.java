package com.cricketbot.service;

import com.cricketbot.entity.CricketNews;
import com.cricketbot.repository.CricketNewsRepository;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class CricketNewsService {

    @Autowired
    private CricketNewsRepository repository;

    @Value("${cricket.rss.url}")
    private String rssUrl;

    private static final Logger log = LoggerFactory.getLogger(CricketNewsService.class);

    public int fetchAndSaveNews() {
        int savedCount = 0;

        try {
            log.info("🔍 Fetching RSS feed from: {}", rssUrl);

            // Parse RSS feed
            URL feedUrl = new URL(rssUrl);

            SyndFeedInput input = new SyndFeedInput();

            SyndFeed feed = input.build(new XmlReader(feedUrl));

            log.info("📰 Found {} articles in feed", feed.getEntries().size());

            // Process each entry
            for (SyndEntry entry : feed.getEntries()) {

                if (entry.getPublishedDate() == null)
                    continue;

                LocalDate publishedDate = convertToLocalDateTime(entry.getPublishedDate()).toLocalDate();

                // Skip if not today's article
                if (!publishedDate.equals(LocalDate.now())) {
                    log.debug("⏭️ Skipping article not published today: {}", entry.getTitle());
                    continue;
                }
                // Check if already exists
                if (repository.existsByLink(entry.getLink())) {

                    log.debug("⏭️ Skipping duplicate: {}", entry.getTitle());
                    continue;
                }

                // Create new CricketNews entity
                CricketNews news = new CricketNews();
                news.setTitle(entry.getTitle());

                if (entry.getDescription() != null) {
                    String summary = entry.getDescription().getValue();
                    // Clean HTML tags if present
                    summary = summary.replaceAll("<[^>]*>", "").trim();
                    news.setSummary(summary);
                }

                news.setLink(entry.getLink());

                // Convert Date to LocalDateTime
                if (entry.getPublishedDate() != null) {
                    news.setPublishedDate(convertToLocalDateTime(entry.getPublishedDate()));
                }

                // Save to database
                repository.save(news);
                savedCount++;

                log.info("✅ Saved: {}", news.getTitle());
            }

            log.info("🎉 Successfully saved {} new articles", savedCount);

        } catch (Exception e) {
            log.error("❌ Error fetching RSS: {}", e.getMessage(), e);
        }

        return savedCount;
    }

    /**
     * Get all news from database (sorted by published date, newest first)
     */
    public List<CricketNews> getAllNews() {
        return repository.findAllByOrderByPublishedDateDesc();
    }

    /**
     * Get news that hasn't been posted to Mastodon yet
     */
    public List<CricketNews> getUnpostedNews() {
        return repository.findByPostedFalse();
    }

    /**
     * Get total count of news in database
     */
    public long getNewsCount() {
        return repository.count();
    }

    /**
     * Helper method to convert Date to LocalDateTime
     */
    private LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    @Transactional
    public int deletePostedNews() {
        List<CricketNews> postedNews = repository.findByPostedTrue();
        int count = postedNews.size();
        repository.deleteByPostedTrue();
        return count;
    }

    @Transactional
    public int deleteAllNews() {
        long count = repository.count();
        repository.deleteAllInSingleQuery();
        return (int) count;
    }
}