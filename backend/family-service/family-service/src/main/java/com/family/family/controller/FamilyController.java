
package com.family.family.controller;
import cn.dev33.satoken.annotation.SaCheckLogin;
import com.family.common.core.Result;
import com.family.family.entity.Family;
import com.family.family.entity.FamilyMember;
import com.family.family.service.FamilyService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@SaCheckLogin
@RequestMapping("/api/family")
public class FamilyController {
    
    private final FamilyService familyService;
    
    public FamilyController(FamilyService familyService) {
        this.familyService = familyService;
    }
    
    @PostMapping("/create")
    public Result<Family> create(@RequestBody Family family, @RequestParam Long creatorId) {
        return Result.success(familyService.createFamily(family, creatorId));
    }
    
    @PostMapping("/join")
    public Result<Family> join(@RequestParam String inviteCode, @RequestParam Long userId) {
        return Result.success(familyService.joinFamily(inviteCode, userId));
    }
    
    @GetMapping("/{id}")
    public Result<Family> getById(@PathVariable Long id) {
        return Result.success(familyService.getFamilyById(id));
    }
    
    @GetMapping("/{id}/members")
    public Result<List<FamilyMember>> getMembers(@PathVariable Long id) {
        return Result.success(familyService.getFamilyMembers(id));
    }
    
    @PostMapping("/{id}/remove")
    public Result<Void> removeMember(@PathVariable Long id, @RequestParam Long userId) {
        familyService.removeMember(id, userId);
        return Result.success();
    }
}
