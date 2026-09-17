package com.govscheme.scheme.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SchemeBeneficiaryRepository extends JpaRepository<SchemeBeneficiary, String> {

    List<SchemeBeneficiary> findBySchemeId(String schemeId);

    @Modifying
    @Query("DELETE FROM SchemeBeneficiary b WHERE b.schemeId = :schemeId")
    void deleteBySchemeId(@Param("schemeId") String schemeId);
}
