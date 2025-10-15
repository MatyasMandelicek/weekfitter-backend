package com.weekfitter.weekfitter_backend.respository;

import com.weekfitter.weekfitter_backend.model.Activity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByUserId(Long userId);
}
