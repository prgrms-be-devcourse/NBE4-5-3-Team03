package com.example.Flicktionary.domain.favorite.repository;

import com.example.Flicktionary.domain.favorite.entity.ContentType;
import com.example.Flicktionary.domain.favorite.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findAllByUserId(Long userId);

    boolean existsByUserIdAndContentTypeAndContentId(Long userId, ContentType contentType, Long contentId);
}
