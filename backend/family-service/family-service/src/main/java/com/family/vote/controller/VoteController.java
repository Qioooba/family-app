package com.family.vote.controller;
import cn.dev33.satoken.annotation.SaCheckLogin;
import com.family.common.core.Result;
import com.family.family.entity.Vote;
import com.family.vote.service.VoteService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vote")
@SaCheckLogin
public class VoteController {
    
    private final VoteService voteService;
    
    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }
    
    @GetMapping("/list/{familyId}")
    public Result<List<Vote>> list(@PathVariable Long familyId,
                                   @RequestParam(required = false) Integer status) {
        return Result.success(voteService.getFamilyVotes(familyId, status));
    }
    
    @PostMapping("/create")
    public Result<Vote> create(@RequestBody Vote vote) {
        return Result.success(voteService.createVote(vote));
    }
    
    @PostMapping("/do/{voteId}")
    public Result<Void> doVote(@PathVariable Long voteId,
                               @RequestParam Long userId,
                               @RequestBody List<Integer> options) {
        voteService.vote(voteId, userId, options);
        return Result.success();
    }
    
    @PostMapping("/end/{voteId}")
    public Result<Void> end(@PathVariable Long voteId) {
        voteService.endVote(voteId);
        return Result.success();
    }
    
    @GetMapping("/result/{voteId}")
    public Result<Object> getResult(@PathVariable Long voteId) {
        return Result.success(voteService.getVoteResult(voteId));
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        voteService.removeById(id);
        return Result.success();
    }
}
