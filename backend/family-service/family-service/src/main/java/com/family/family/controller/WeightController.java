package com.family.family.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.family.common.core.Result;
import com.family.family.entity.UserWeight;
import com.family.family.mapper.UserWeightMapper;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/diet/weight")
public class WeightController {
    
    private final UserWeightMapper weightMapper;
    
    public WeightController(UserWeightMapper weightMapper) {
        this.weightMapper = weightMapper;
    }
    
    @PostMapping("/record")
    public Result<Boolean> recordWeight(@RequestBody WeightRecordRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        UserWeight weight = new UserWeight();
        weight.setUserId(userId);
        weight.setWeight(request.getWeight());
        weight.setRecordDate(request.getDate() != null ? request.getDate() : LocalDate.now());
        weight.setNote(request.getNote());
        
        weightMapper.insert(weight);
        return Result.success(true);
    }
    
    @GetMapping("/history")
    public Result<List<UserWeight>> getWeightHistory() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(weightMapper.selectByUserId(userId));
    }
    
    @GetMapping("/latest")
    public Result<UserWeight> getLatestWeight() {
        Long userId = StpUtil.getLoginIdAsLong();
        List<UserWeight> list = weightMapper.selectByUserId(userId);
        return Result.success(list.isEmpty() ? null : list.get(0));
    }
    
    public static class WeightRecordRequest {
        private BigDecimal weight;
        private LocalDate date;
        private String note;
        
        public BigDecimal getWeight() { return weight; }
        public void setWeight(BigDecimal weight) { this.weight = weight; }
        
        public LocalDate getDate() { return date; }
        public void setDate(LocalDate date) { this.date = date; }
        
        public String getNote() { return note; }
        public void setNote(String note) { this.note = note; }
    }
}
