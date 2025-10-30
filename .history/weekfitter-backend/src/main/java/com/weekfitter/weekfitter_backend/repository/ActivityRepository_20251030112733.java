package com.weekfitter.weekfitter_backend.repository;

import com.weekfitter.weekfitter_backend.model.Activity;
import com.weekfitter.weekfitter_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, UUID> {
    List<Activity> findByUser(User user);
}
