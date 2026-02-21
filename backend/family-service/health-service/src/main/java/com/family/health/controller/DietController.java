package com.family.health.controller;

import com.family.common.core.Result;
import com.family.health.entity.DietRecord;
import com.family.health.service.DietService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/diet")
@RequiredArgsConstructor
public class DietController {
    
    private final DietService dietService;
    
    @PostMapping("/record")
    public Result<DietRecord> addRecord(@RequestBody DietRecord record) {
        return Result.success(dietService.addRecord(record));
    }
    
    @GetMapping("/day/{userId}")
    public Result<List<DietRecord>> getDayRecords(@PathVariable Long userId,
                                                   @RequestParam String date) {
        return Result.success(dietService.getDayRecords(userId, LocalDate.parse(date)));
    }
    
    @GetMapping("/statistics/day/{userId}")
    public Result<Map<String, Object>> getDayStatistics(@PathVariable Long userId,
                                                         @RequestParam String date) {
        return Result.success(dietService.getDayStatistics(userId, LocalDate.parse(date)));
    }
    
    @GetMapping("/statistics/week/{userId}")
    public Result<Map<String, Object>> getWeekStatistics(@PathVariable Long userId) {
        return Result.success(dietService.getWeekStatistics(userId));
    }
    
    @PostMapping("/recognize")
    public Result<List<DietRecord>> recognizeFood(@RequestParam String imageBase64) {
        return Result.success(dietService.recognizeFood(imageBase64));
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        dietService.removeById(id);
        return Result.success();
    }
}
