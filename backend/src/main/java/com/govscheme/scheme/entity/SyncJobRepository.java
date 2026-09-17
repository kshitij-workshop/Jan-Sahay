package com.govscheme.scheme.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SyncJobRepository extends JpaRepository<SyncJob, String> {
}
