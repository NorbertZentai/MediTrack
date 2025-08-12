package hu.project.MediWeb.modules.GoogleImage.service;

import hu.project.MediWeb.modules.GoogleImage.config.GoogleConfig;
import hu.project.MediWeb.modules.GoogleImage.dto.GoogleImageResult;
import hu.project.MediWeb.modules.GoogleImage.dto.GoogleSearchResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Comparator;

@Service
public class GoogleImageService {

    private final WebClient webClient;
    private final GoogleConfig googleConfig;

    public GoogleImageService(GoogleConfig googleConfig) {
        this.googleConfig = googleConfig;
        this.webClient = WebClient.builder()
                .baseUrl("https://www.googleapis.com/customsearch/v1")
                .build();
    }

    public Mono<GoogleImageResult> searchImages(String query) {
        // Check if Google API credentials are configured
        if (googleConfig.getKey() == null || googleConfig.getKey().isEmpty() ||
            googleConfig.getCx() == null || googleConfig.getCx().isEmpty()) {
            System.out.println("🔍 [GOOGLE-IMG] API credentials not configured, skipping image search for: " + query);
            return Mono.just(null);
        }

        System.out.println("🔍 [GOOGLE-IMG] Searching images for: " + query);
        return webClient.get()
                .uri(uriBuilder ->
                        uriBuilder
                                .queryParam("key", googleConfig.getKey())
                                .queryParam("cx", googleConfig.getCx())
                                .queryParam("searchType", "image")
                                .queryParam("num", 5)
                                .queryParam("q", query)
                                .build()
                )
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(GoogleSearchResponse.class)
                .map(resp -> {
                    System.out.println("✅ [GOOGLE-IMG] Successfully found images for: " + query);
                    return resp.items().stream()
                            .map(item -> new GoogleImageResult(item.title(), item.link()))
                            .max(Comparator.comparingInt(image -> score(query, image)))
                            .orElse(null);
                })
                .onErrorResume(error -> {
                    System.err.println("❌ [GOOGLE-IMG] Error searching images for: " + query);
                    System.err.println("❌ [GOOGLE-IMG] Error: " + error.getMessage());
                    return Mono.just(null);
                });
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
}
