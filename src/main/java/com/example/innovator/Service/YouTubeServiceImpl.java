package com.example.innovator.Service;

import com.example.innovator.DTO.YouTubeDTO;
import com.example.innovator.Entity.YouTubeEntity;
import com.example.innovator.Repository.YouTubeRepository;
import org.springframework.beans.factory.annotation.Value;
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
    public List<YouTubeDTO> getChannelsByKeyword(String keyword) {
        URI uri = UriComponentsBuilder.fromHttpUrl(youtubeApiUrl)
                .queryParam("part", "snippet")
                .queryParam("q", keyword)
                .queryParam("type", "video")
                .queryParam("maxResults", 50) // 결과를 50개까지 가져옴
                .queryParam("regionCode", "KR") // 한국 지역 코드
                .queryParam("relevanceLanguage", "ko") // 한국어 콘텐츠 우선
                .queryParam("key", apiKey)
                .build()
                .toUri();

        // JSON 응답을 Map으로 받기
        Map<String, Object> response = restTemplate.getForObject(uri, Map.class);
        List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");

        // 채널 ID 추출
        List<String> channelIds = items.stream()
                .map(item -> {
                    Map<String, Object> snippet = (Map<String, Object>) item.get("snippet");
                    return (String) snippet.get("channelId");
                })
                .collect(Collectors.toList());

        return getChannelsWithSubscriberCount(channelIds);
    }

    private List<YouTubeDTO> getChannelsWithSubscriberCount(List<String> channelIds) {
        URI uri = UriComponentsBuilder.fromHttpUrl(youtubeChannelApiUrl)
                .queryParam("part", "statistics,snippet")
                .queryParam("id", String.join(",", channelIds))
                .queryParam("key", apiKey)
                .build()
                .toUri();

        // JSON 응답을 Map으로 받기
        Map<String, Object> response = restTemplate.getForObject(uri, Map.class);
        List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");

        // 구독자 수 필터링 및 DTO 변환
        List<YouTubeEntity> channels = items.stream()
                .filter(item -> {
                    Map<String, Object> statistics = (Map<String, Object>) item.get("statistics");
                    int subscriberCount = Integer.parseInt((String) statistics.get("subscriberCount"));
                    return subscriberCount >= 2000 && subscriberCount <= 50000; // 구독자 수 조건을 확장
                })
                // YouTubeEntity 생성 시 keyword 추가
                .map(item -> {
                    Map<String, Object> snippet = (Map<String, Object>) item.get("snippet");
                    String title = (String) snippet.get("title");
                    Map<String, Object> statistics = (Map<String, Object>) item.get("statistics");
                    int subscriberCount = Integer.parseInt((String) statistics.get("subscriberCount"));
                    return new YouTubeEntity(null, title, "keyword", subscriberCount); // keyword를 추가
                })
                .collect(Collectors.toList());

        // 채널 정보를 데이터베이스에 저장
        youTubeRepository.saveAll(channels);

        // DTO로 변환하여 반환
        return channels.stream()
                .map(YouTubeDTO::entityToDto)
                .collect(Collectors.toList());
    }
}