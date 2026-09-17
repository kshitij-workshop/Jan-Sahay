package com.govscheme.scheme.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchemeCategoryRepository extends JpaRepository<SchemeCategory, String> {

    @Query("SELECT DISTINCT c.category FROM SchemeCategory c")
    List<String> findDistinctCategories();

    List<SchemeCategory> findBySchemeId(String schemeId);

    @Modifying
    @Query("DELETE FROM SchemeCategory c WHERE c.schemeId = :schemeId")
    void deleteBySchemeId(@Param("schemeId") String schemeId);
}
