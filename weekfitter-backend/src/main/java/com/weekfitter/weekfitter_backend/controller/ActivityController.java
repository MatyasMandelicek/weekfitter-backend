package com.weekfitter.weekfitter_backend.controller;

import com.weekfitter.weekfitter_backend.model.Activity;
import com.weekfitter.weekfitter_backend.service.ActivityService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/activities")
@CrossOrigin(origins = "*")
public class ActivityController {

    private final ActivityService service;

    public ActivityController(ActivityService service) {
        this.service = service;
    }

    @GetMapping("/{userId}")
    public List<Activity> getUserActivities(@PathVariable Long userId) {
        return service.getActivitiesByUser(userId);
    }

    @PostMapping
    public Activity addActivity(@RequestBody Activity activity) {
        return service.addActivity(activity);
    }

    @DeleteMapping("/{id}")
    public void deleteActivity(@PathVariable Long id) {
        service.deleteActivity(id);
    }
}
