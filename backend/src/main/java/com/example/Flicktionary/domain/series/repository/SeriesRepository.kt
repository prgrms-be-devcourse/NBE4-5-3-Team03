package com.example.Flicktionary.domain.series.repository

import com.example.Flicktionary.domain.series.entity.Series
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface SeriesRepository : JpaRepository<Series, Long> {
    @Query(
        ("SELECT DISTINCT s FROM Series s " +
                "LEFT JOIN FETCH s.casts c " +
                "LEFT JOIN FETCH c.actor a " +
                "LEFT JOIN FETCH s.director d " +
                "WHERE s._id = :id")
    )
    fun findByIdWithCastsAndDirector(id: Long): Series?

    @Query("SELECT s FROM Series s WHERE s.title LIKE CONCAT('%', :keyword, '%')")
    fun findByTitleLike(@Param("keyword") keyword: String, pageable: Pageable): Page<Series>

    fun findByDirectorId(directorId: Long): List<Series>
}