package com.cricketbot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;

import com.cricketbot.entity.CricketNews;

import jakarta.transaction.Transactional;

@Repository
public interface CricketNewsRepository extends JpaRepository<CricketNews, Long> {

    boolean existsByLink(String link);

    List<CricketNews> findByPostedFalse();

    List<CricketNews> findAllByOrderByPublishedDateDesc();

    CricketNews findFirstByPostedFalseOrderByPublishedDateAsc();

    List<CricketNews> findByPostedTrue();

    void deleteByPostedTrue();

    @Query("SELECT c.link FROM CricketNews c")
    List<String> findAllLinks();

    @Modifying
    @Transactional
    @Query("DELETE FROM CricketNews")
    void deleteAllInSingleQuery();

}
