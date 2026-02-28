package com.family.family.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.family.family.entity.MindMatchQuestion;
import com.family.family.entity.MindMatchSession;
import com.family.family.mapper.MindMatchQuestionMapper;
import com.family.family.mapper.MindMatchSessionMapper;
import com.family.family.service.MindMatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 默契问答服务实现
 */
@Service
public class MindMatchServiceImpl implements MindMatchService {
    
    @Autowired
    private MindMatchQuestionMapper questionMapper;
    
    @Autowired
    private MindMatchSessionMapper sessionMapper;
    
    // 预设问题列表
    private static final List<Map<String, Object>> DEFAULT_QUESTIONS = Arrays.asList(
        // 夫妻类问题
        Map.of("question", "老婆最喜欢吃什么？", "type", "spouse", "difficulty", 1),
        Map.of("question", "老公最想去哪里旅游？", "type", "spouse", "difficulty", 2),
        Map.of("question", "老婆最喜欢的颜色是什么？", "type", "spouse", "difficulty", 1),
        Map.of("question", "老公最喜欢的电影是什么？", "type", "spouse", "difficulty", 2),
        Map.of("question", "老婆最想要的生日礼物是什么？", "type", "spouse", "difficulty", 3),
        Map.of("question", "老公最大的爱好是什么？", "type", "spouse", "difficulty", 2),
        Map.of("question", "老婆最喜欢什么花？", "type", "spouse", "difficulty", 1),
        Map.of("question", "老公最害怕什么？", "type", "spouse", "difficulty", 2),
        Map.of("question", "老婆最喜欢什么运动？", "type", "spouse", "difficulty", 1),
        Map.of("question", "我们第一次约会在哪里？", "type", "spouse", "difficulty", 3),
        
        // 亲子类问题
        Map.of("question", "妈妈最喜欢的歌是什么？", "type", "parent", "difficulty", 1),
        Map.of("question", "爸爸最擅长的菜是什么？", "type", "parent", "difficulty", 2),
        Map.of("question", "妈妈最常用的app是什么？", "type", "parent", "difficulty", 1),
        Map.of("question", "爸爸的爱好是什么？", "type", "parent", "difficulty", 1),
        Map.of("question", "妈妈最想要什么？", "type", "parent", "difficulty", 2),
        Map.of("question", "爸爸年轻时的梦想是什么？", "type", "parent", "difficulty", 3),
        Map.of("question", "妈妈最喜欢的电视剧是什么？", "type", "parent", "difficulty", 2),
        Map.of("question", "爸爸最崇拜的人是谁？", "type", "parent", "difficulty", 3),
        
        // 家庭类问题
        Map.of("question", "全家一起去过最远的地方是哪里？", "type", "family", "difficulty", 2),
        Map.of("question", "家里谁的厨艺最好？", "type", "family", "difficulty", 1),
        Map.of("question", "周末家人最喜欢做什么？", "type", "family", "difficulty", 1),
        Map.of("question", "家里谁睡觉最晚？", "type", "family", "difficulty", 1),
        Map.of("question", "家里谁最早起床？", "type", "family", "difficulty", 1),
        Map.of("question", "年夜饭必吃的一道菜是什么？", "type", "family", "difficulty", 2),
        Map.of("question", "家庭聚会必玩什么游戏？", "type", "family", "difficulty", 2),
        Map.of("question", "谁是这个家最聪明的人？", "type", "family", "difficulty", 1)
    );
    
    @Override
    public void initQuestions() {
        // 检查是否已有问题
        Long count = questionMapper.selectCount(null);
        if (count > 0) {
            return; // 已有数据，不重复初始化
        }
        
        // 插入预设问题
        for (Map<String, Object> q : DEFAULT_QUESTIONS) {
            MindMatchQuestion question = new MindMatchQuestion();
            question.setQuestion((String) q.get("question"));
            question.setType((String) q.get("type"));
            question.setDifficulty((Integer) q.get("difficulty"));
            question.setStatus(1);
            question.setPlayCount(0);
            question.setCorrectCount(0);
            question.setCreateTime(LocalDateTime.now());
            question.setUpdateTime(LocalDateTime.now());
            question.setIsDeleted(0);
            questionMapper.insert(question);
        }
    }
    
    @Override
    public MindMatchQuestion getRandomQuestion(String type) {
        LambdaQueryWrapper<MindMatchQuestion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MindMatchQuestion::getStatus, 1);
        wrapper.eq(MindMatchQuestion::getIsDeleted, 0);
        if (type != null && !type.isEmpty()) {
            wrapper.eq(MindMatchQuestion::getType, type);
        }
        wrapper.orderByDesc(MindMatchQuestion::getPlayCount);
        
        List<MindMatchQuestion> questions = questionMapper.selectList(wrapper);
        if (questions == null || questions.isEmpty()) {
            return null;
        }
        
        // 随机选择一个题目
        Random random = new Random();
        MindMatchQuestion question = questions.get(random.nextInt(questions.size()));
        
        // 更新使用次数
        question.setPlayCount(question.getPlayCount() + 1);
        questionMapper.updateById(question);
        
        // 返回题目(隐藏正确答案)
        question.setTargetAnswer(null);
        return question;
    }
    
    @Override
    public MindMatchSession createSession(Long familyId, Long player1Id, Long player2Id) {
        MindMatchSession session = new MindMatchSession();
        session.setFamilyId(familyId);
        session.setPlayer1Id(player1Id);
        session.setPlayer2Id(player2Id);
        session.setRound(1);
        session.setPlayer1Answered(false);
        session.setPlayer2Answered(false);
        session.setStatus("waiting");
        session.setCreateTime(LocalDateTime.now());
        session.setUpdateTime(LocalDateTime.now());
        session.setIsDeleted(0);
        
        // 获取一个随机问题
        MindMatchQuestion question = getRandomQuestion(null);
        if (question != null) {
            session.setQuestionId(question.getId());
        }
        
        sessionMapper.insert(session);
        return session;
    }
    
    @Override
    public MindMatchSession answerQuestion(Long sessionId, Long playerId, String answer) {
        MindMatchSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            return null;
        }
        
        boolean isPlayer1 = playerId.equals(session.getPlayer1Id());
        
        if (isPlayer1) {
            session.setPlayer1Answer(answer);
            session.setPlayer1Answered(true);
        } else {
            session.setPlayer2Answer(answer);
            session.setPlayer2Answered(true);
        }
        
        // 检查是否两人都已回答
        if (session.getPlayer1Answered() && session.getPlayer2Answered()) {
            // 判断是否匹配
            String ans1 = session.getPlayer1Answer() != null ? session.getPlayer1Answer().trim() : "";
            String ans2 = session.getPlayer2Answer() != null ? session.getPlayer2Answer().trim() : "";
            
            boolean isMatch = ans1.equalsIgnoreCase(ans2);
            session.setIsMatch(isMatch);
            session.setRoundPoints(isMatch ? 10 : 5);
            session.setStatus("completed");
            
            // 更新题目的正确率
            if (session.getQuestionId() != null) {
                MindMatchQuestion question = questionMapper.selectById(session.getQuestionId());
                if (question != null) {
                    question.setCorrectCount(question.getCorrectCount() + (isMatch ? 1 : 0));
                    questionMapper.updateById(question);
                }
            }
        } else {
            session.setStatus("in_progress");
        }
        
        session.setUpdateTime(LocalDateTime.now());
        sessionMapper.updateById(session);
        return session;
    }
    
    @Override
    public MindMatchSession getSessionById(Long sessionId) {
        return sessionMapper.selectById(sessionId);
    }
    
    @Override
    public List<Map<String, Object>> getFamilyGameHistory(Long familyId) {
        LambdaQueryWrapper<MindMatchSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MindMatchSession::getFamilyId, familyId);
        wrapper.eq(MindMatchSession::getIsDeleted, 0);
        wrapper.orderByDesc(MindMatchSession::getCreateTime);
        wrapper.last("LIMIT 20");
        
        List<MindMatchSession> sessions = sessionMapper.selectList(wrapper);
        
        return sessions.stream().map(s -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", s.getId());
            map.put("round", s.getRound());
            map.put("questionId", s.getQuestionId());
            map.put("player1Id", s.getPlayer1Id());
            map.put("player2Id", s.getPlayer2Id());
            map.put("isMatch", s.getIsMatch());
            map.put("roundPoints", s.getRoundPoints());
            map.put("status", s.getStatus());
            map.put("createTime", s.getCreateTime());
            return map;
        }).collect(Collectors.toList());
    }
    
    @Override
    public Map<String, Object> getUserGameStats(Long userId) {
        LambdaQueryWrapper<MindMatchSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.eq(MindMatchSession::getPlayer1Id, userId).or().eq(MindMatchSession::getPlayer2Id, userId));
        wrapper.eq(MindMatchSession::getIsDeleted, 0);
        
        List<MindMatchSession> sessions = sessionMapper.selectList(wrapper);
        
        int totalGames = sessions.size();
        int matchCount = (int) sessions.stream().filter(s -> Boolean.TRUE.equals(s.getIsMatch())).count();
        int totalPoints = sessions.stream().mapToInt(s -> s.getRoundPoints() != null ? s.getRoundPoints() : 0).sum();
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalGames", totalGames);
        stats.put("matchCount", matchCount);
        stats.put("totalPoints", totalPoints);
        stats.put("matchRate", totalGames > 0 ? Math.round(matchCount * 100.0 / totalGames) : 0);
        
        return stats;
    }
    
    @Override
    public Map<String, Object> getMatchRate(Long familyId) {
        LambdaQueryWrapper<MindMatchSession> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MindMatchSession::getFamilyId, familyId);
        wrapper.eq(MindMatchSession::getIsDeleted, 0);
        wrapper.isNotNull(MindMatchSession::getIsMatch);
        
        List<MindMatchSession> sessions = sessionMapper.selectList(wrapper);
        
        int totalGames = sessions.size();
        int matchCount = (int) sessions.stream().filter(s -> Boolean.TRUE.equals(s.getIsMatch())).count();
        
        Map<String, Object> result = new HashMap<>();
        result.put("totalGames", totalGames);
        result.put("matchCount", matchCount);
        result.put("matchRate", totalGames > 0 ? Math.round(matchCount * 100.0 / totalGames) : 0);
        
        return result;
    }
}
