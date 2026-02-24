
package com.cricketbot.service;

import com.cricketbot.entity.CricketNews;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import okhttp3.*;

import org.springframework.beans.factory.annotation.Value;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@Service
public class MastodonService {

    @Value("${mastodon.instance.url}")
    private String instanceUrl;

    @Value("${mastodon.access.token}")
    private String accessToken;

    private static final Logger log = LoggerFactory.getLogger(MastodonService.class);

    private static final String API_ENDPOINT = "/api/v1/statuses";
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();

    /**
     * Post a toot (message) to Mastodon
     * 
     * @param message The message to post
     * @return The posted status ID
     */
    public String postToot(String message) throws IOException {


        log.info("📤 Posting to Mastodon...");

        // Build request body
        RequestBody body = new FormBody.Builder()
                .add("status", message)
                .add("visibility", "public")
                .build();

        // Build HTTP request
        Request request = new Request.Builder()
                .url(instanceUrl + API_ENDPOINT)
                .header("Authorization", "Bearer " + accessToken)
                .post(body)
                .build();


        // Execute request
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Failed to post: " + response.code() + " - " + response.body().string());
            }

            JsonObject json = gson.fromJson(response.body().string(), JsonObject.class);
            String statusId = json.get("id").getAsString();

            log.info("✅ Posted to Mastodon! Status ID: {}", statusId);
            return statusId;
        }
    }

    /**
     * Format and post cricket news to Mastodon
     * 
     * @param news The cricket news to post
     */
    public void postCricketNews(CricketNews news) throws IOException {
        String toot = formatToot(news);
        postToot(toot);
    }

    /**
     * Format cricket news into a toot (max 500 characters)
     */
    private String formatToot(CricketNews news) {
        StringBuilder toot = new StringBuilder();

        // Add emoji and title
        toot.append("🏏 ").append(news.getTitle()).append("\n\n");

        // Add summary (if available)
        if (news.getSummary() != null && !news.getSummary().isEmpty()) {
            String summary = news.getSummary();

            // Ensure total length stays under 500 chars
            int maxSummaryLength = 500 - toot.length() - news.getLink().length() - 20;

            if (summary.length() > maxSummaryLength) {
                summary = summary.substring(0, maxSummaryLength - 3) + "...";
            }

            toot.append(summary).append("\n\n");
        }

        // Add link
        toot.append(news.getLink()).append("\n\n");

        // Add hashtags
        toot.append("#Cricket #CricketNews");

        return toot.toString();
    }
}