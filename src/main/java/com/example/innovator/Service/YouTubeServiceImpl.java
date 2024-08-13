package com.example.innovator.Service;

import com.example.innovator.DTO.YouTubeDTO;
import com.example.innovator.Entity.MonetizationStatus;
import com.example.innovator.Entity.YouTubeEntity;
import com.example.innovator.Repository.YouTubeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.ZonedDateTime;

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
    public List<YouTubeDTO> getChannelsByKeyword(String keyword) {
        URI uri = UriComponentsBuilder.fromHttpUrl(youtubeApiUrl)
                .queryParam("part", "snippet")
                .queryParam("q", keyword)
                .queryParam("type", "video")
                .queryParam("maxResults", 25)
                .queryParam("regionCode", "KR")
                .queryParam("relevanceLanguage", "ko")
                .queryParam("key", apiKey)
                .build()
                .toUri();

        Map<String, Object> response = restTemplate.getForObject(uri, Map.class);
        List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");

        List<String> channelIds = items.stream()
                .map(item -> {
                    Map<String, Object> snippet = (Map<String, Object>) item.get("snippet");
                    return (String) snippet.get("channelId");
                })
                .collect(Collectors.toList());

        return getChannelsWithSubscriberCount(channelIds, keyword);
    }

    private List<YouTubeDTO> getChannelsWithSubscriberCount(List<String> channelIds, String keyword) {
        URI uri = UriComponentsBuilder.fromHttpUrl(youtubeChannelApiUrl)
                .queryParam("part", "statistics,snippet")
                .queryParam("id", String.join(",", channelIds))
                .queryParam("key", apiKey)
                .build()
                .toUri();

        Map<String, Object> response = restTemplate.getForObject(uri, Map.class);
        List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");

        DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

        List<YouTubeEntity> channels = items.stream()
                .filter(item -> {
                    Map<String, Object> statistics = (Map<String, Object>) item.get("statistics");
                    int subscriberCount = Integer.parseInt((String) statistics.get("subscriberCount"));
                    return subscriberCount >= 0 && subscriberCount <= 2000;
                })
                .map(item -> {
                    Map<String, Object> snippet = (Map<String, Object>) item.get("snippet");
                    String title = (String) snippet.get("title");
                    String publishedAt = (String) snippet.get("publishedAt");

                    LocalDate creationDate = ZonedDateTime.parse(publishedAt, formatter).toLocalDate();

                    Map<String, Object> statistics = (Map<String, Object>) item.get("statistics");
                    int subscriberCount = Integer.parseInt((String) statistics.get("subscriberCount"));

                    // 구독자 수 1000명 이상 + 생성일로부터 1년 이상이면 수익 창출 채널
                    MonetizationStatus monetizationStatus = (subscriberCount >= 1000 && creationDate.isBefore(LocalDate.now().minusYears(1)))
                            ? MonetizationStatus.MONETIZED
                            : MonetizationStatus.NOT_MONETIZED;

                    return new YouTubeEntity(null, title, keyword, subscriberCount, monetizationStatus, creationDate);
                })
                .collect(Collectors.toList());

        youTubeRepository.saveAll(channels);

        return channels.stream()
                .map(YouTubeDTO::entityToDto)
                .collect(Collectors.toList());
    }
}
