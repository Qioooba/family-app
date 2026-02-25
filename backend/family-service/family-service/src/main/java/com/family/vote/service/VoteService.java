package com.family.vote.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.family.family.entity.Vote;

import java.util.List;

public interface VoteService extends IService<Vote> {
    
    List<Vote> getFamilyVotes(Long familyId, Integer status);
    
    Vote createVote(Vote vote);
    
    void vote(Long voteId, Long userId, List<Integer> options);
    
    void endVote(Long voteId);
    
    Object getVoteResult(Long voteId);
}
