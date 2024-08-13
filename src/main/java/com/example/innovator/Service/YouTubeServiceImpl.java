package com.example.innovator.Service;

import com.example.innovator.DTO.YouTubeDTO;
import com.example.innovator.Entity.YouTubeEntity;
import com.example.innovator.Repository.YouTubeRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class YouTubeServiceImpl implements YouTubeService {

    private final String apiKey = "YOUR_API_KEY";
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
                .queryParam("maxResults", 50)
                .queryParam("key", apiKey)
                .build()
                .toUri();

        YouTubeSearchResponse response = restTemplate.getForObject(uri, YouTubeSearchResponse.class);

        // 영상 ID로 채널 정보를 조회
        List<String> channelIds = response.getItems().stream()
                .map(item -> item.getSnippet().getChannelId())
                .collect(Collectors.toList());

        return getChannelsWithSubscriberCount(channelIds);
    }

    private List<YouTubeDTO> getChannelsWithSubscriberCount(List<String> channelIds) {
        URI uri = UriComponentsBuilder.fromHttpUrl(youtubeChannelApiUrl)
                .queryParam("part", "statistics")
                .queryParam("id", String.join(",", channelIds))
                .queryParam("key", apiKey)
                .build()
                .toUri();

        YouTubeChannelResponse response = restTemplate.getForObject(uri, YouTubeChannelResponse.class);

        List<YouTubeEntity> channels = response.getItems().stream()
                .filter(item -> {
                    int subscriberCount = item.getStatistics().getSubscriberCount();
                    return subscriberCount >= 5000 && subscriberCount <= 20000;
                })
                .map(item -> new YouTubeEntity(null, item.getSnippet().getTitle(), item.getStatistics().getSubscriberCount()))
                .collect(Collectors.toList());

        // Save channels to database
        youTubeRepository.saveAll(channels);

        // Convert to DTO and return
        return channels.stream()
                .map(YouTubeDTO::entityToDto)
                .collect(Collectors.toList());
    }
}


