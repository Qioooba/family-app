package com.family.family.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.family.family.entity.ChallengeCheckin;
import com.family.family.entity.ChallengeParticipant;
import com.family.family.entity.FamilyChallenge;
import com.family.family.entity.GameQuiz;
import com.family.family.entity.GameRiddle;
import com.family.family.mapper.ChallengeCheckinMapper;
import com.family.family.mapper.ChallengeParticipantMapper;
import com.family.family.mapper.FamilyChallengeMapper;
import com.family.family.mapper.GameQuizMapper;
import com.family.family.mapper.GameRiddleMapper;
import com.family.family.service.GameService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 游戏服务实现
 */
@Service
public class GameServiceImpl implements GameService {
    
    private final FamilyChallengeMapper familyChallengeMapper;
    private final ChallengeParticipantMapper challengeParticipantMapper;
    private final ChallengeCheckinMapper challengeCheckinMapper;
    private final GameRiddleMapper gameRiddleMapper;
    private final GameQuizMapper gameQuizMapper;
    
    public GameServiceImpl(FamilyChallengeMapper familyChallengeMapper,
                          ChallengeParticipantMapper challengeParticipantMapper,
                          ChallengeCheckinMapper challengeCheckinMapper,
                          GameRiddleMapper gameRiddleMapper,
                          GameQuizMapper gameQuizMapper) {
        this.familyChallengeMapper = familyChallengeMapper;
        this.challengeParticipantMapper = challengeParticipantMapper;
        this.challengeCheckinMapper = challengeCheckinMapper;
        this.gameRiddleMapper = gameRiddleMapper;
        this.gameQuizMapper = gameQuizMapper;
    }
    
    @Override
    public FamilyChallenge createChallenge(FamilyChallenge challenge) {
        challenge.setParticipantCount(0);
        challenge.setStatus(1);
        challenge.setCreateTime(LocalDateTime.now());
        challenge.setUpdateTime(LocalDateTime.now());
        familyChallengeMapper.insert(challenge);
        return challenge;
    }
    
    @Override
    public FamilyChallenge updateChallenge(FamilyChallenge challenge) {
        challenge.setUpdateTime(LocalDateTime.now());
        familyChallengeMapper.updateById(challenge);
        return challenge;
    }
    
    @Override
    public void deleteChallenge(Long challengeId) {
        FamilyChallenge challenge = familyChallengeMapper.selectById(challengeId);
        if (challenge != null) {
            challenge.setStatus(0);
            challenge.setUpdateTime(LocalDateTime.now());
            familyChallengeMapper.updateById(challenge);
        }
    }
    
    @Override
    public FamilyChallenge getChallengeById(Long challengeId) {
        return familyChallengeMapper.selectById(challengeId);
    }
    
    @Override
    public List<FamilyChallenge> getFamilyChallenges(Long familyId) {
        QueryWrapper<FamilyChallenge> wrapper = new QueryWrapper<>();
        wrapper.eq("family_id", familyId)
               .eq("status", 1)
               .orderByDesc("create_time");
        return familyChallengeMapper.selectList(wrapper);
    }
    
    @Override
    public List<FamilyChallenge> getActiveChallenges(Long familyId) {
        QueryWrapper<FamilyChallenge> wrapper = new QueryWrapper<>();
        wrapper.eq("family_id", familyId)
               .eq("status", 1)
               .ge("end_date", LocalDate.now())
               .le("start_date", LocalDate.now())
               .orderByDesc("create_time");
        return familyChallengeMapper.selectList(wrapper);
    }
    
    @Override
    public ChallengeParticipant joinChallenge(Long challengeId, Long userId) {
        // 检查是否已参与
        ChallengeParticipant existing = getParticipant(challengeId, userId);
        if (existing != null) {
            return existing;
        }
        
        FamilyChallenge challenge = familyChallengeMapper.selectById(challengeId);
        if (challenge == null) {
            return null;
        }
        
        ChallengeParticipant participant = new ChallengeParticipant();
        participant.setChallengeId(challengeId);
        participant.setFamilyId(challenge.getFamilyId());
        participant.setUserId(userId);
        participant.setCurrentValue(new java.math.BigDecimal("0"));
        participant.setProgress(0);
        participant.setCheckinCount(0);
        participant.setIsCompleted(0);
        participant.setStatus(1);
        participant.setCreateTime(LocalDateTime.now());
        participant.setUpdateTime(LocalDateTime.now());
        challengeParticipantMapper.insert(participant);
        
        // 更新参与人数
        challenge.setParticipantCount(challenge.getParticipantCount() + 1);
        challenge.setUpdateTime(LocalDateTime.now());
        familyChallengeMapper.updateById(challenge);
        
        return participant;
    }
    
    @Override
    public void leaveChallenge(Long challengeId, Long userId) {
        ChallengeParticipant participant = getParticipant(challengeId, userId);
        if (participant != null) {
            participant.setStatus(0);
            participant.setUpdateTime(LocalDateTime.now());
            challengeParticipantMapper.updateById(participant);
            
            // 更新参与人数
            FamilyChallenge challenge = familyChallengeMapper.selectById(challengeId);
            if (challenge != null && challenge.getParticipantCount() > 0) {
                challenge.setParticipantCount(challenge.getParticipantCount() - 1);
                challenge.setUpdateTime(LocalDateTime.now());
                familyChallengeMapper.updateById(challenge);
            }
        }
    }
    
    @Override
    public List<ChallengeParticipant> getChallengeParticipants(Long challengeId) {
        QueryWrapper<ChallengeParticipant> wrapper = new QueryWrapper<>();
        wrapper.eq("challenge_id", challengeId)
               .eq("status", 1)
               .orderByDesc("progress");
        return challengeParticipantMapper.selectList(wrapper);
    }
    
    @Override
    public ChallengeParticipant getParticipant(Long challengeId, Long userId) {
        QueryWrapper<ChallengeParticipant> wrapper = new QueryWrapper<>();
        wrapper.eq("challenge_id", challengeId)
               .eq("user_id", userId)
               .eq("status", 1);
        return challengeParticipantMapper.selectOne(wrapper);
    }
    
    @Override
    public ChallengeCheckin checkin(ChallengeCheckin checkin) {
        checkin.setStatus(1);
        checkin.setCreateTime(LocalDateTime.now());
        checkin.setUpdateTime(LocalDateTime.now());
        challengeCheckinMapper.insert(checkin);
        
        // 更新参与者信息
        ChallengeParticipant participant = challengeParticipantMapper.selectById(checkin.getParticipantId());
        if (participant != null) {
            participant.setCheckinCount(participant.getCheckinCount() + 1);
            participant.setLastCheckinDate(LocalDate.now());
            participant.setUpdateTime(LocalDateTime.now());
            challengeParticipantMapper.updateById(participant);
        }
        
        return checkin;
    }
    
    @Override
    public List<ChallengeCheckin> getCheckinRecords(Long challengeId, Long userId) {
        QueryWrapper<ChallengeCheckin> wrapper = new QueryWrapper<>();
        wrapper.eq("challenge_id", challengeId)
               .eq("user_id", userId)
               .eq("status", 1)
               .orderByDesc("create_time");
        return challengeCheckinMapper.selectList(wrapper);
    }
    
    @Override
    public Integer getConsecutiveCheckinDays(Long challengeId, Long userId) {
        // 简化实现，实际应该按日期计算连续天数
        List<ChallengeCheckin> records = getCheckinRecords(challengeId, userId);
        return records.size();
    }
    
    @Override
    public GameRiddle getRandomRiddle() {
        QueryWrapper<GameRiddle> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1)
               .last("ORDER BY RAND() LIMIT 1");
        return gameRiddleMapper.selectOne(wrapper);
    }
    
    @Override
    public GameRiddle getRiddleById(Long id) {
        return gameRiddleMapper.selectById(id);
    }
    
    @Override
    public boolean verifyRiddleAnswer(Long riddleId, String answer) {
        GameRiddle riddle = gameRiddleMapper.selectById(riddleId);
        if (riddle == null) {
            return false;
        }
        boolean correct = riddle.getAnswer().equalsIgnoreCase(answer.trim());
        
        // 更新统计
        riddle.setPlayCount(riddle.getPlayCount() + 1);
        if (correct) {
            riddle.setCorrectCount(riddle.getCorrectCount() + 1);
        }
        riddle.setUpdateTime(LocalDateTime.now());
        gameRiddleMapper.updateById(riddle);
        
        return correct;
    }
    
    @Override
    public List<GameRiddle> getRiddlesByCategory(String category) {
        QueryWrapper<GameRiddle> wrapper = new QueryWrapper<>();
        wrapper.eq("category", category)
               .eq("status", 1);
        return gameRiddleMapper.selectList(wrapper);
    }
    
    @Override
    public GameQuiz getRandomQuiz() {
        QueryWrapper<GameQuiz> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1)
               .last("ORDER BY RAND() LIMIT 1");
        return gameQuizMapper.selectOne(wrapper);
    }
    
    @Override
    public GameQuiz getQuizById(Long id) {
        return gameQuizMapper.selectById(id);
    }
    
    @Override
    public boolean verifyQuizAnswer(Long quizId, String answer) {
        GameQuiz quiz = gameQuizMapper.selectById(quizId);
        if (quiz == null) {
            return false;
        }
        boolean correct = quiz.getCorrectAnswer().equalsIgnoreCase(answer.trim());
        
        // 更新统计
        quiz.setPlayCount(quiz.getPlayCount() + 1);
        if (correct) {
            quiz.setCorrectCount(quiz.getCorrectCount() + 1);
        }
        quiz.setUpdateTime(LocalDateTime.now());
        gameQuizMapper.updateById(quiz);
        
        return correct;
    }
    
    @Override
    public List<GameQuiz> getQuizzesByCategory(String category) {
        QueryWrapper<GameQuiz> wrapper = new QueryWrapper<>();
        wrapper.eq("category", category)
               .eq("status", 1);
        return gameQuizMapper.selectList(wrapper);
    }
    
    @Override
    public List<Map<String, Object>> getChallengeRank(Long challengeId) {
        List<ChallengeParticipant> participants = getChallengeParticipants(challengeId);
        List<Map<String, Object>> rank = new ArrayList<>();
        
        int rankNum = 1;
        for (ChallengeParticipant p : participants) {
            Map<String, Object> item = new HashMap<>();
            item.put("rank", rankNum++);
            item.put("userId", p.getUserId());
            item.put("progress", p.getProgress());
            item.put("checkinCount", p.getCheckinCount());
            item.put("isCompleted", p.getIsCompleted());
            rank.add(item);
        }
        
        return rank;
    }
}
