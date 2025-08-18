package hu.project.MediWeb.modules.GoogleImage.service;

import hu.project.MediWeb.modules.GoogleImage.config.GoogleConfig;
import hu.project.MediWeb.modules.GoogleImage.dto.GoogleImageResult;
import hu.project.MediWeb.modules.GoogleImage.dto.GoogleSearchResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import java.util.Comparator;

@Service
public class GoogleImageService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final GoogleConfig googleConfig;
    private static final Logger log = LoggerFactory.getLogger(GoogleImageService.class);

    public GoogleImageService(GoogleConfig googleConfig) { this.googleConfig = googleConfig; }

    @PostConstruct
    public void init() {
        log.info("[GOOGLE-IMG] init keyPresent={} cxPresent={}", safeHas(googleConfig.getKey()), safeHas(googleConfig.getCx()));
    }

    public GoogleImageResult searchImages(String query) {
        log.debug("[GOOGLE-IMG] search query='{}' keyPresent={} cxPresent={}", query, safeHas(googleConfig.getKey()), safeHas(googleConfig.getCx()));
        if (missing(googleConfig.getKey()) || missing(googleConfig.getCx())) {
            log.warn("[GOOGLE-IMG] credentials missing query='{}'", query);
            return null;
        }
        try {
            String url = String.format("https://www.googleapis.com/customsearch/v1?key=%s&cx=%s&searchType=image&num=5&q=%s",
                    googleConfig.getKey(), googleConfig.getCx(), encode(query));
            GoogleSearchResponse resp = restTemplate.getForObject(url, GoogleSearchResponse.class);
            if (resp == null || resp.items() == null) {
                log.info("[GOOGLE-IMG] no results query='{}'", query);
                return null;
            }
            GoogleImageResult best = resp.items().stream()
                    .map(item -> new GoogleImageResult(item.title(), item.link()))
                    .max(Comparator.comparingInt(image -> score(query, image)))
                    .orElse(null);
            log.info("[GOOGLE-IMG] success query='{}' found={} ", query, best != null);
            return best;
        } catch (Exception e) {
            log.error("[GOOGLE-IMG] error query='{}' msg={}", query, e.getMessage());
            return null;
        }
    }

    private int score(String query, GoogleImageResult image) {
        String lowerTitle = image.title().toLowerCase();
        String[] queryWords = query.toLowerCase().split("\\s+");
        int score = 0;

        for (String word : queryWords) {
            if (lowerTitle.contains(word)) {
                score += 10;
            }
        }

        if (lowerTitle.contains("banner")) score -= 10;
        if (lowerTitle.contains("hirdetés") || lowerTitle.contains("promo")) score -= 5;
        if (lowerTitle.contains("100ml")) score += 3;
        if (lowerTitle.contains("szuszpenzió")) score += 3;

        return score;
    }

    private boolean missing(String v) { return v == null || v.isEmpty(); }
    private boolean safeHas(String v) { return v != null && !v.isEmpty(); }
    private String encode(String q) { return q.replace(" ", "+"); }
}
