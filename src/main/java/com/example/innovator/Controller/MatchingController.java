package com.example.innovator.Controller;

import com.example.innovator.Service.MatchingService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class MatchingController {

    private final MatchingService matchingService;

    public MatchingController(MatchingService matchingService) {
        this.matchingService = matchingService;
    }

    @PostMapping("/match/recommendation")
    public List<Map<String, Object>> matchYouTubersWithRecommendations(@RequestBody Map<String, String> request) {
        String keyword = request.get("keyword");
        return matchingService.matchYouTubersWithRecommendations(keyword);
    }
}
