package com.govscheme.scheme.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SchemeRawDataRepository extends JpaRepository<SchemeRawData, String> {

    Optional<SchemeRawData> findBySlug(String slug);
}
