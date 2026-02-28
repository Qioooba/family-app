package com.family.family.service;

import com.family.family.entity.MindMatchQuestion;
import com.family.family.entity.MindMatchSession;

import java.util.List;
import java.util.Map;

/**
 * 默契问答服务接口
 */
public interface MindMatchService {
    
    /**
     * 初始化问题库(预设问题)
     */
    void initQuestions();
    
    /**
     * 获取随机问题
     */
    MindMatchQuestion getRandomQuestion(String type);
    
    /**
     * 创建游戏会话
     */
    MindMatchSession createSession(Long familyId, Long player1Id, Long player2Id);
    
    /**
     * 回答问题
     */
    MindMatchSession answerQuestion(Long sessionId, Long playerId, String answer);
    
    /**
     * 获取会话详情
     */
    MindMatchSession getSessionById(Long sessionId);
    
    /**
     * 获取家庭游戏历史
     */
    List<Map<String, Object>> getFamilyGameHistory(Long familyId);
    
    /**
     * 获取用户游戏统计
     */
    Map<String, Object> getUserGameStats(Long userId);
    
    /**
     * 获取默契值(匹配率)
     */
    Map<String, Object> getMatchRate(Long familyId);
}
