package com.shreeai.os.platform.controller;

import com.shreeai.os.platform.planner.TaskItem;
import com.shreeai.os.platform.planner.TaskPlannerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/agent/planner")
public class TaskPlannerController {

    private final TaskPlannerService plannerService;

    public TaskPlannerController(TaskPlannerService plannerService) {
        this.plannerService = plannerService;
    }

    @PostMapping("/plan")
    public List<TaskItem> plan(@RequestBody String goal) throws Exception {
        return plannerService.planTasks(goal);
    }
}
