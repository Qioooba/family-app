package com.family.anniversary.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.family.anniversary.entity.Anniversary;
import com.family.anniversary.service.AnniversaryService;
import com.family.common.core.Result;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/anniversary")
@SaCheckLogin
public class AnniversaryController {

    private final AnniversaryService anniversaryService;

    public AnniversaryController(AnniversaryService anniversaryService) {
        this.anniversaryService = anniversaryService;
    }

    @GetMapping("/list")
    public Result<List<Anniversary>> list(@RequestParam Long familyId) {
        return Result.success(anniversaryService.listByFamilyId(familyId));
    }

    @PostMapping("/create")
    public Result<Boolean> create(@RequestBody Anniversary anniversary) {
        Long userId = StpUtil.getLoginIdAsLong();
        anniversary.setUserId(userId);
        return Result.success(anniversaryService.save(anniversary));
    }

    @PutMapping("/update")
    public Result<Boolean> update(@RequestBody Anniversary anniversary) {
        return Result.success(anniversaryService.updateById(anniversary));
    }

    @DeleteMapping("/delete/{id}")
    public Result<Boolean> delete(@PathVariable Long id) {
        return Result.success(anniversaryService.removeById(id));
    }
}
