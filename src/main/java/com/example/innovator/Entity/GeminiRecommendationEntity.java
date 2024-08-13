package com.example.innovator.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

@Entity(name = "gemini_recommendations")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class GeminiRecommendationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String channelName;
    private String recommendationType;
    private String description;
}
