package com.family.vote.service;

import com.family.vote.entity.Vote;
import com.family.vote.entity.VoteRecord;

import java.util.List;
import java.util.Map;

public interface VoteService {
    
    List<Vote> getFamilyVotes(Long familyId);
    
    Vote createVote(Vote vote);
    
    VoteRecord castVote(Long voteId, Long userId, String userName, String selectedOptions);
    
    Map<String, Object> getVoteResult(Long voteId);
}
