package com.govscheme.profile.entity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, String> {

    List<UserAddress> findByUserId(String userId);

    void deleteByUserId(String userId);
}
