package com.govscheme.eligibility.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SchemeEligibilityRepository extends JpaRepository<SchemeEligibility, String> {
}
