package com.example.Flicktionary.domain.favorite.service;

import com.example.Flicktionary.domain.favorite.entity.Favorite;
import com.example.Flicktionary.domain.favorite.entity.FavoriteDto;
import com.example.Flicktionary.domain.favorite.repository.FavoriteRepository;
import com.example.Flicktionary.domain.user.entity.User;
import com.example.Flicktionary.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FavoriteService {
    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;

    @Transactional
    public FavoriteDto createFavorite(FavoriteDto favoriteDto) {
        User user = userRepository.findById(favoriteDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Favorite favorite = favoriteDto.toEntity(user);

        favoriteRepository.save(favorite);
        return FavoriteDto.fromEntity(favorite);
    }

    public List<FavoriteDto> getUserFavorites(Long userId) {
        List<Favorite> favorites = favoriteRepository.findAllByUserId(userId);
        return favorites.stream()
                .map(FavoriteDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteFavorite(Long id) {
        favoriteRepository.deleteById(id);
    }

}
