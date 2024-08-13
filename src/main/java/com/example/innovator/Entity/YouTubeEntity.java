package com.example.innovator.Entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDate;

@Entity(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class YouTubeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name; // 유튜브 채널 명
    private String keyword; // 검색 키워드
    private int subscriberCount; // 해당 유튜브 채널 구독자 수
    private MonetizationStatus monetizationStatus; // 수익 창출 여부
    private LocalDate channelCreationDate; // 계정 생성일
}
