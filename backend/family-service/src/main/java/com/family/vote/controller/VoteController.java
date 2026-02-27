package com.family.vote.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.family.common.core.Result;
import com.family.vote.entity.Vote;
import com.family.vote.service.VoteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@SaCheckLogin
@RequestMapping("/api/vote")
public class VoteController {
    
    private final VoteService voteService;
    
    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }
    
    @GetMapping("/list/{familyId}")
    public Result<List<Vote>> list(@PathVariable Long familyId) {
        return Result.success(voteService.getFamilyVotes(familyId));
    }
    
    @PostMapping("/create")
    public Result<Vote> create(@RequestBody Vote vote) {
        return Result.success(voteService.createVote(vote));
    }
    
    @PostMapping("/{id}/vote")
    public Result<?> vote(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        try {
            Long userId = Long.valueOf(params.get("userId").toString());
            String userName = params.get("userName") != null ? params.get("userName").toString() : "用户";
            String selectedOptions = params.get("selectedOptions").toString();
            voteService.castVote(id, userId, userName, selectedOptions);
            return Result.success("投票成功");
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    
    @GetMapping("/{id}/result")
    public Result<Map<String, Object>> result(@PathVariable Long id) {
        return Result.success(voteService.getVoteResult(id));
    }
}
