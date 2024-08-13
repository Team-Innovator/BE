package com.example.innovator.Controller;

import com.example.innovator.DTO.YouTubeDTO;
import com.example.innovator.Service.YouTubeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class YouTubeController {

    private final YouTubeService youTubeService;

    public YouTubeController(YouTubeService youTubeService) {
        this.youTubeService = youTubeService;
    }

    @GetMapping("/youtube/channels")
    public List<YouTubeDTO> getChannels(@RequestParam String keyword) {
        return youTubeService.getChannelsByKeyword(keyword);
    }
}
