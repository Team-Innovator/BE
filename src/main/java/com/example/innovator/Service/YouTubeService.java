package com.example.innovator.Service;

import com.example.innovator.DTO.YouTubeDTO;

import java.util.List;

public interface YouTubeService {
    List<YouTubeDTO> getChannelsByKeyword(String keyword);
}
