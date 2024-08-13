package com.example.innovator.DTO;

import com.example.innovator.Entity.MonetizationStatus;
import com.example.innovator.Entity.YouTubeEntity;
import lombok.*;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class YouTubeDTO {
    private Long id;
    private String name;
    private String keyword;
    private int subscriberCount;
    private MonetizationStatus monetizationStatus;
    private LocalDate channelCreationDate;

    public static YouTubeDTO entityToDto(YouTubeEntity youTubeEntity) {
        return new YouTubeDTO(
                youTubeEntity.getId(),
                youTubeEntity.getName(),
                youTubeEntity.getKeyword(),
                youTubeEntity.getSubscriberCount(),
                youTubeEntity.getMonetizationStatus(),
                youTubeEntity.getChannelCreationDate()
        );
    }

    public YouTubeEntity dtoToEntity(){
        return new YouTubeEntity(id, name, keyword, subscriberCount, monetizationStatus, channelCreationDate);
    }
}
