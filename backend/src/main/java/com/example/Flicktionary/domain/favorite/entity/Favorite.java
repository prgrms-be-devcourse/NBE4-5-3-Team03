package com.example.Flicktionary.domain.favorite.entity;

import com.example.Flicktionary.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
@Table(name = "favorite", uniqueConstraints = {
        @UniqueConstraint(name = "unique_favorite", columnNames = {"user_id", "content_type", "content_id"})
})
public class Favorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ContentType contentType; // MOVIE, SERIES 구분

    @Column(nullable = false)
    private Long contentId; // 영화 or 드라마 ID
}
