package com.govscheme.scheme.entity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SchemeRepository extends JpaRepository<Scheme, String> {

    Optional<Scheme> findBySlug(String slug);

    boolean existsBySlug(String slug);

    Page<Scheme> findAll(Pageable pageable);
}
