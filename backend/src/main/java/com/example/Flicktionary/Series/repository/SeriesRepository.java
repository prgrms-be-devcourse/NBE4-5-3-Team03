package com.example.Flicktionary.Series.repository;

import com.example.Flicktionary.Series.domain.Series;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeriesRepository extends JpaRepository<Series, Integer> {
}
