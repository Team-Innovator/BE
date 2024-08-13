package com.example.innovator.DTO;

import com.example.innovator.Entity.GeminiRecommendationEntity;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class GeminiRecommendationDTO {
    private Long id;
    private String channelName;
    private String recommendationType;
    private String description;

    // 새로운 생성자 추가
    public GeminiRecommendationDTO(String recommendationType, String description) {
        this.recommendationType = recommendationType;
        this.description = description;
    }

    public static GeminiRecommendationDTO entityToDto(GeminiRecommendationEntity geminiRecommendationEntity){
        return new GeminiRecommendationDTO(
                geminiRecommendationEntity.getId(),
                geminiRecommendationEntity.getChannelName(),
                geminiRecommendationEntity.getRecommendationType(),
                geminiRecommendationEntity.getDescription()
        );
    }

    public GeminiRecommendationEntity dtoToEntity(){
        return new GeminiRecommendationEntity(id, channelName, recommendationType, description);
    }
}
