package com.weekfitter.weekfitter_backend.service;

import com.weekfitter.weekfitter_backend.model.Activity;
import com.weekfitter.weekfitter_backend.respository.ActivityRepository;

import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ActivityService {
    private final ActivityRepository activityRepository;

    public ActivityService(ActivityRepository activityRepository) {
        this.activityRepository = activityRepository;
    }

    public List<Activity> getActivitiesByUser(Long userId) {
        return activityRepository.findByUserId(userId);
    }

    public Activity addActivity(Activity activity) {
        return activityRepository.save(activity);
    }

    public void deleteActivity(Long id) {
        activityRepository.deleteById(id);
    }
}
