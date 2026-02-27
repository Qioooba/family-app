package com.family.vote.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.vote.entity.Vote;
import com.family.vote.entity.VoteRecord;
import com.family.vote.mapper.VoteMapper;
import com.family.vote.mapper.VoteRecordMapper;
import com.family.vote.service.VoteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VoteServiceImpl extends ServiceImpl<VoteMapper, Vote> implements VoteService {
    
    private final VoteRecordMapper voteRecordMapper;
    
    public VoteServiceImpl(VoteRecordMapper voteRecordMapper) {
        this.voteRecordMapper = voteRecordMapper;
    }
    
    @Override
    public List<Vote> getFamilyVotes(Long familyId) {
        LambdaQueryWrapper<Vote> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Vote::getFamilyId, familyId)
               .orderByDesc(Vote::getCreateTime);
        return list(wrapper);
    }
    
    @Override
    @Transactional
    public Vote createVote(Vote vote) {
        vote.setCreateTime(LocalDateTime.now());
        vote.setUpdateTime(LocalDateTime.now());
        if (vote.getValidDays() != null && vote.getValidDays() > 0) {
            vote.setDeadline(LocalDateTime.now().plusDays(vote.getValidDays()));
        }
        save(vote);
        return vote;
    }
    
    @Override
    @Transactional
    public VoteRecord castVote(Long voteId, Long userId, String userName, String selectedOptions) {
        // 检查是否已投票
        LambdaQueryWrapper<VoteRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VoteRecord::getVoteId, voteId)
               .eq(VoteRecord::getUserId, userId);
        if (voteRecordMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("您已投过票");
        }
        
        VoteRecord record = new VoteRecord();
        record.setVoteId(voteId);
        record.setUserId(userId);
        record.setUserName(userName);
        record.setSelectedOptions(selectedOptions);
        record.setCreateTime(LocalDateTime.now());
        voteRecordMapper.insert(record);
        return record;
    }
    
    @Override
    public Map<String, Object> getVoteResult(Long voteId) {
        Vote vote = getById(voteId);
        if (vote == null) {
            throw new RuntimeException("投票不存在");
        }
        
        // 获取所有投票记录
        LambdaQueryWrapper<VoteRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(VoteRecord::getVoteId, voteId);
        List<VoteRecord> records = voteRecordMapper.selectList(wrapper);
        
        // 解析选项
        String optionsStr = vote.getOptions();
        List<String> options = parseJsonArray(optionsStr);
        
        // 统计每个选项的票数
        Map<String, Long> optionCounts = new LinkedHashMap<>();
        for (String opt : options) {
            optionCounts.put(opt, 0L);
        }
        
        // 统计投票用户
        List<Map<String, Object>> voters = new ArrayList<>();
        
        for (VoteRecord record : records) {
            List<String> selected = parseJsonArray(record.getSelectedOptions());
            for (String sel : selected) {
                optionCounts.merge(sel, 1L, Long::sum);
            }
            
            Map<String, Object> voter = new HashMap<>();
            voter.put("userId", record.getUserId());
            voter.put("userName", record.getUserName());
            voter.put("selectedOptions", selected);
            voter.put("createTime", record.getCreateTime());
            voters.add(voter);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("vote", vote);
        result.put("optionCounts", optionCounts);
        result.put("totalVotes", records.size());
        result.put("voters", voters);
        
        return result;
    }
    
    private List<String> parseJsonArray(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            // 简单解析JSON数组
            json = json.trim();
            if (json.startsWith("[")) {
                json = json.substring(1);
            }
            if (json.endsWith("]")) {
                json = json.substring(0, json.length() - 1);
            }
            return Arrays.stream(json.split(","))
                    .map(s -> s.trim().replace("\"", ""))
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
