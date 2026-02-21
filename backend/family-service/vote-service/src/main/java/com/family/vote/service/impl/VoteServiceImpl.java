package com.family.vote.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.vote.entity.Vote;
import com.family.vote.mapper.VoteMapper;
import com.family.vote.service.VoteService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class VoteServiceImpl extends ServiceImpl<VoteMapper, Vote> implements VoteService {
    
    @Override
    public List<Vote> getFamilyVotes(Long familyId, Integer status) {
        return lambdaQuery()
                .eq(Vote::getFamilyId, familyId)
                .eq(status != null, Vote::getStatus, status)
                .orderByDesc(Vote::getCreateTime)
                .list();
    }
    
    @Override
    public Vote createVote(Vote vote) {
        vote.setStatus(0);
        save(vote);
        return vote;
    }
    
    @Override
    public void vote(Long voteId, Long userId, List<Integer> options) {
        // TODO: 保存投票记录到vote_record表
        // 检查是否已投票，是否可改票等
    }
    
    @Override
    public void endVote(Long voteId) {
        Vote vote = getById(voteId);
        vote.setStatus(1);
        vote.setEndTime(LocalDateTime.now());
        updateById(vote);
    }
    
    @Override
    public Object getVoteResult(Long voteId) {
        // TODO: 统计投票结果
        Map<String, Object> result = new HashMap<>();
        result.put("voteId", voteId);
        result.put("totalVotes", 10);
        return result;
    }
}
