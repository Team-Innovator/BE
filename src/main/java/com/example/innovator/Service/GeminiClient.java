package com.example.innovator.Service;

import com.example.innovator.DTO.GeminiRecommendationDTO;
import com.example.innovator.Entity.GeminiRecommendationEntity;
import com.example.innovator.Repository.GeminiRecommendationRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class GeminiClient {

    @Value("${gemini.api-key}")
    private String apiKey;
    private final String geminiApiUrl = "https://api.gemini.com/v1/";

    private final RestTemplate restTemplate;
    private final GeminiRecommendationRepository repository;

    public GeminiClient(RestTemplate restTemplate, GeminiRecommendationRepository repository) {
        this.restTemplate = restTemplate;
        this.repository = repository;
    }

    public GeminiRecommendationDTO getRecommendation(String youtubeChannelName) {
        String encodedChannelName = "";
        try {
            encodedChannelName = URLEncoder.encode(youtubeChannelName, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            System.err.println("Error encoding channel name: " + e.getMessage());
        }

        URI uri = UriComponentsBuilder.fromHttpUrl(geminiApiUrl + "recommendation")
                .queryParam("channelName", encodedChannelName)
                .queryParam("key", apiKey)
                .build()
                .toUri();

        System.out.println("Requesting Gemini API with URI: " + uri);

        GeminiRecommendationDTO response = null;
        try {
            response = restTemplate.getForObject(uri, GeminiRecommendationDTO.class);
        } catch (HttpClientErrorException e) {
            System.err.println("HTTP Error during Gemini API request: " + e.getMessage());
            response = new GeminiRecommendationDTO("Unknown", "No recommendation available");
        } catch (Exception e) {
            System.err.println("Unexpected Error during Gemini API request: " + e.getMessage());
            response = new GeminiRecommendationDTO("Unknown", "No recommendation available");
        }

        if (response != null) {
            GeminiRecommendationEntity entity = GeminiRecommendationEntity.builder()
                    .channelName(youtubeChannelName)
                    .recommendationType(response.getRecommendationType())
                    .description(response.getDescription())
                    .build();

            repository.save(entity);
        }

        return response;
    }
}
