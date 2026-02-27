package com.family.calendar.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.family.common.core.Result;
import com.family.calendar.entity.Anniversary;
import com.family.calendar.service.AnniversaryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 纪念日控制器
 * 使用 /api/anniversary 路径
 */
@RestController
@SaCheckLogin
@RequestMapping("/api/anniversary")
public class AnniversaryApiController {
    
    private final AnniversaryService anniversaryService;
    
    public AnniversaryApiController(AnniversaryService anniversaryService) {
        this.anniversaryService = anniversaryService;
    }
    
    @GetMapping("/list/{familyId}")
    public Result<List<Anniversary>> list(@PathVariable Long familyId) {
        return Result.success(anniversaryService.getFamilyAnniversaries(familyId));
    }
    
    @PostMapping("/create")
    public Result<Anniversary> create(@RequestBody Anniversary anniversary) {
        return Result.success(anniversaryService.createAnniversary(anniversary));
    }
}
