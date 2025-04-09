package com.example.Flicktionary.domain.movie.repository

import com.example.Flicktionary.domain.movie.entity.Movie
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface MovieRepository : JpaRepository<Movie, Long> {
    @Query("SELECT m FROM Movie m WHERE LOWER(REPLACE(m.title, ' ', '')) LIKE CONCAT('%', :keyword, '%')")
    fun findByTitleLike(keyword: String, pageable: Pageable): Page<Movie>

    @Query(
        ("SELECT DISTINCT m FROM Movie m " +
                "LEFT JOIN FETCH m.casts c " +
                "LEFT JOIN FETCH c.actor a " +
                "LEFT JOIN FETCH m.director d " +
                "WHERE m._id = :id")
    )
    fun findByIdWithCastsAndDirector(id: Long): Movie?

    fun findByDirectorId(directorId: Long): List<Movie>
    
    fun existsMovieByTitleAndReleaseDate(title: String, releaseDate: LocalDate?): Boolean
}
