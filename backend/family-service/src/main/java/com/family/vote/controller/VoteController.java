package com.family.vote.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.family.common.core.Result;
import com.family.vote.entity.Vote;
import com.family.vote.entity.VoteRecord;
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
    public Result<VoteRecord> vote(@PathVariable Long id, @RequestBody Map<String, Object> params) {
        Long userId = Long.valueOf(params.get("userId").toString());
        String userName = params.get("userName").toString();
        String selectedOptions = params.get("selectedOptions").toString();
        return Result.success(voteService.castVote(id, userId, userName, selectedOptions));
    }
    
    @GetMapping("/{id}/result")
    public Result<Map<String, Object>> result(@PathVariable Long id) {
        return Result.success(voteService.getVoteResult(id));
    }
}
