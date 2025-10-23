package com.weekfitter.weekfitter_backend.service;

import com.weekfitter.weekfitter_backend.model.Activity;
import com.weekfitter.weekfitter_backend.model.User;
import com.weekfitter.weekfitter_backend.respository.ActivityRepository;
import com.weekfitter.weekfitter_backend.respository.UserRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ActivityService {

    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;

    public ActivityService(ActivityRepository activityRepository, UserRepository userRepository) {
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
    }

    public List<Activity> getAllActivities() {
        return activityRepository.findAll();
    }

    public Optional<Activity> getActivityById(UUID id) {
        return activityRepository.findById(id);
    }

    public List<Activity> getActivitiesByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return activityRepository.findByUser(user);
    }

    public Activity createActivity(Activity activity) {
        return activityRepository.save(activity);
    }

    public Activity updateActivity(Long id, Activity updatedActivity) {
        return activityRepository.findById(id)
                .map(activity -> {
                    activity.setName(updatedActivity.getName());
                    activity.setDescription(updatedActivity.getDescription());
                    activity.setDurationMinutes(updatedActivity.getDurationMinutes());
                    activity.setDistanceKm(updatedActivity.getDistanceKm());
                    activity.setCaloriesBurned(updatedActivity.getCaloriesBurned());
                    return activityRepository.save(activity);
                })
                .orElseThrow(() -> new RuntimeException("Activity not found"));
    }

    public void deleteActivity(Long id) {
        activityRepository.deleteById(id);
    }
}
