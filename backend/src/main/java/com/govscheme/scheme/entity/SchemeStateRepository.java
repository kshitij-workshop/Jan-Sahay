package com.govscheme.scheme.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchemeStateRepository extends JpaRepository<SchemeState, String> {

    List<SchemeState> findBySchemeId(String schemeId);

    @Modifying
    @Query("DELETE FROM SchemeState s WHERE s.schemeId = :schemeId")
    void deleteBySchemeId(@Param("schemeId") String schemeId);
}
