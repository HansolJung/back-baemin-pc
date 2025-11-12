package it.korea.app_bmpc.popular.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import it.korea.app_bmpc.popular.entity.PopularKeywordEntity;

public interface PopularKeywordRepository extends JpaRepository<PopularKeywordEntity, Integer> {

    Optional<PopularKeywordEntity> findByKeyword(String keyword);
}
