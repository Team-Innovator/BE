package com.example.innovator.Service;

import com.example.innovator.DTO.YouTubeDTO;
import com.example.innovator.Entity.YouTubeEntity;
import com.example.innovator.Repository.YouTubeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class YouTubeServiceImpl implements YouTubeService {

    @Value("${youtube.api-key}")
    private String apiKey;
    private final String youtubeApiUrl = "https://www.googleapis.com/youtube/v3/search";
    private final String youtubeChannelApiUrl = "https://www.googleapis.com/youtube/v3/channels";

    private final RestTemplate restTemplate;
    private final YouTubeRepository youTubeRepository;

    public YouTubeServiceImpl(RestTemplate restTemplate, YouTubeRepository youTubeRepository) {
        this.restTemplate = restTemplate;
        this.youTubeRepository = youTubeRepository;
    }

    @Override
    @Cacheable(value = "youtubeChannels", key = "#keyword")
    public List<YouTubeDTO> getChannelsByKeyword(String keyword) {
        // 필요한 데이터만 요청
        URI uri = UriComponentsBuilder.fromHttpUrl(youtubeApiUrl)
                .queryParam("part", "snippet")
                .queryParam("q", keyword)
                .queryParam("type", "video")
                .queryParam("maxResults", 25) // 비용을 줄이기 위해 적절한 maxResults 사용
                .queryParam("regionCode", "KR")
                .queryParam("relevanceLanguage", "ko")
                .queryParam("key", apiKey)
                .build()
                .toUri();

        // JSON 응답을 Map으로 받기
        Map<String, Object> response = restTemplate.getForObject(uri, Map.class);
        List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");

        List<String> channelIds = items.stream()
                .map(item -> {
                    Map<String, Object> snippet = (Map<String, Object>) item.get("snippet");
                    return (String) snippet.get("channelId");
                })
                .distinct() // 중복 제거
                .collect(Collectors.toList());

        return getChannelsWithSubscriberCount(channelIds, keyword);
    }

    private List<YouTubeDTO> getChannelsWithSubscriberCount(List<String> channelIds, String keyword) {
        // 필요한 데이터만 요청
        URI uri = UriComponentsBuilder.fromHttpUrl(youtubeChannelApiUrl)
                .queryParam("part", "statistics,snippet")
                .queryParam("id", String.join(",", channelIds))
                .queryParam("key", apiKey)
                .build()
                .toUri();

        // JSON 응답을 Map으로 받기
        Map<String, Object> response = restTemplate.getForObject(uri, Map.class);
        List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");

        List<YouTubeEntity> channels = items.stream()
                .filter(item -> {
                    Map<String, Object> statistics = (Map<String, Object>) item.get("statistics");
                    int subscriberCount = Integer.parseInt((String) statistics.get("subscriberCount"));
                    return subscriberCount >= 2000 && subscriberCount <= 50000;
                })
                .map(item -> {
                    Map<String, Object> snippet = (Map<String, Object>) item.get("snippet");
                    String title = (String) snippet.get("title");
                    Map<String, Object> statistics = (Map<String, Object>) item.get("statistics");
                    int subscriberCount = Integer.parseInt((String) statistics.get("subscriberCount"));
                    return new YouTubeEntity(null, title, keyword, subscriberCount);
                })
                .collect(Collectors.toList());

        youTubeRepository.saveAll(channels);

        return channels.stream()
                .map(YouTubeDTO::entityToDto)
                .collect(Collectors.toList());
    }
}
