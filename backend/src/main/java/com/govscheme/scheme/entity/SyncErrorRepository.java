package com.govscheme.scheme.entity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SyncErrorRepository extends JpaRepository<SyncError, String> {

    Page<SyncError> findByJobId(String jobId, Pageable pageable);
}
