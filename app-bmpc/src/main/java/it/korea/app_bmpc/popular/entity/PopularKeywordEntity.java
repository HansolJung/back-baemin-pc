package it.korea.app_bmpc.popular.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "bmpc_popular_keyword")
public class PopularKeywordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int popularId;
    @Column(unique = true)
    private String keyword;
    private int searchCount;
    private LocalDateTime lastSearchDate;

    public void incrementSearchCount() {
        this.searchCount++;
        this.lastSearchDate = LocalDateTime.now();
    }
}
