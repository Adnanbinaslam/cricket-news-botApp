package com.cricketbot.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "cricket_news"
)
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CricketNews {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @Column(nullable = false, length = 500)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String summary;

   @Column(nullable = false, unique = true, length = 1000)
    private String link;

    private LocalDateTime publishedDate;

    @Column(nullable = false)
    private boolean posted;

    @CreationTimestamp
    private LocalDateTime createdAt;
    
}
