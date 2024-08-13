package com.example.innovator.DTO;

import com.example.innovator.Entity.YouTubeEntity;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class YouTubeDTO {
    private Long id;
    private String name;
    private int subscriberCount;

    public static YouTubeDTO entityToDto(YouTubeEntity youTubeEntity) {
        return new YouTubeDTO(
                youTubeEntity.getId(),
                youTubeEntity.getName(),
                youTubeEntity.getSubscriberCount()
        );
    }

    public YouTubeEntity dtoToEntity(){
        return new YouTubeEntity(id, name, subscriberCount);
    }
}
