package com.govscheme.scheme.entity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchemeRepository extends JpaRepository<Scheme, String>, JpaSpecificationExecutor<Scheme> {

    @Query("SELECT DISTINCT s.schemeCategory FROM Scheme s WHERE s.schemeCategory IS NOT NULL")
    List<String> findDistinctCategories();

    Optional<Scheme> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Page<Scheme> findAll(Pageable pageable);
}
