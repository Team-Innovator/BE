package com.example.innovator.Repository;

import com.example.innovator.Entity.YouTubeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface YouTubeRepository extends JpaRepository<YouTubeEntity, Long> {
    List<YouTubeEntity> findBySubscriberCountBetween(int minSubscriberCount, int maxSubscriberCount);
}
