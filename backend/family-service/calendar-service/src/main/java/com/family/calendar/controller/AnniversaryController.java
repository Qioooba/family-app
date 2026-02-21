package com.family.calendar.controller;

import com.family.common.core.Result;
import com.family.calendar.entity.Anniversary;
import com.family.calendar.service.AnniversaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/anniversary")
@RequiredArgsConstructor
public class AnniversaryController {
    
    private final AnniversaryService anniversaryService;
    
    @GetMapping("/list/{familyId}")
    public Result<List<Anniversary>> list(@PathVariable Long familyId) {
        return Result.success(anniversaryService.getFamilyAnniversaries(familyId));
    }
    
    @GetMapping("/upcoming/{familyId}")
    public Result<List<Anniversary>> getUpcoming(@PathVariable Long familyId,
                                                    @RequestParam(defaultValue = "30") int days) {
        return Result.success(anniversaryService.getUpcomingAnniversaries(familyId, days));
    }
    
    @PostMapping("/create")
    public Result<Anniversary> create(@RequestBody Anniversary anniversary) {
        return Result.success(anniversaryService.createAnniversary(anniversary));
    }
    
    @GetMapping("/today/{familyId}")
    public Result<List<Anniversary>> getToday(@PathVariable Long familyId) {
        return Result.success(anniversaryService.getTodayCountdown(familyId));
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        anniversaryService.removeById(id);
        return Result.success();
    }
}
