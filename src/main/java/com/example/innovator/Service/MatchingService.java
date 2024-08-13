package com.example.innovator.Service;

import com.example.innovator.DTO.GeminiRecommendationDTO;
import com.example.innovator.DTO.YouTubeDTO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class MatchingService {

    private final GeminiClient geminiClient;
    private final YouTubeServiceImpl youTubeService;

    public MatchingService(GeminiClient geminiClient, YouTubeServiceImpl youTubeService) {
        this.geminiClient = geminiClient;
        this.youTubeService = youTubeService;
    }

    public List<Map<String, Object>> matchYouTubersWithRecommendations(String keyword) {
        List<YouTubeDTO> channels = youTubeService.getChannelsByKeyword(keyword);

        return channels.stream().map(channel -> {
            GeminiRecommendationDTO recommendation = geminiClient.getRecommendation(channel.getName());
            return Map.of(
                    "channelInfo", channel,
                    "recommendation", recommendation
            );
        }).collect(Collectors.toList());
    }
}
