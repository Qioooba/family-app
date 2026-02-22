package com.family.task.controller;

import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/task")
public class TaskController {

    @GetMapping("/list")
    public List<Object> list() {
        return new ArrayList<>();
    }

    @PostMapping
    public Object add() {
        return null;
    }
}
