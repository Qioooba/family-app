package com.family.family.controller;

import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/family")
public class FamilyController {

    @GetMapping("/list")
    public List<Object> list() {
        return new ArrayList<>();
    }

    @GetMapping("/info")
    public Object info() {
        return null;
    }
}
