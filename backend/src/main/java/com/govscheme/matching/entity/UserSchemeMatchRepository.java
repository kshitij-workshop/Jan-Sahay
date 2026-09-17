package com.govscheme.matching.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSchemeMatchRepository extends JpaRepository<UserSchemeMatch, String> {

    List<UserSchemeMatch> findByUserId(String userId);

    List<UserSchemeMatch> findByUserIdAndStatus(String userId, UserSchemeMatch.Status status);

    Optional<UserSchemeMatch> findByUserIdAndSchemeId(String userId, String schemeId);

    List<UserSchemeMatch> findBySchemeId(String schemeId);

    long countByUserIdAndStatus(String userId, UserSchemeMatch.Status status);
}
