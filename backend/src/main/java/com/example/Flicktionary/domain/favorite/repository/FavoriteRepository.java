package com.example.Flicktionary.domain.favorite.repository;

import com.example.Flicktionary.domain.favorite.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
}
