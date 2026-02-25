package com.family.family.service;

import com.family.family.entity.ChallengeCheckin;
import com.family.family.entity.ChallengeParticipant;
import com.family.family.entity.FamilyChallenge;
import com.family.family.entity.GameQuiz;
import com.family.family.entity.GameRiddle;

import java.util.List;
import java.util.Map;

/**
 * 游戏服务
 */
public interface GameService {
    
    // 挑战管理
    FamilyChallenge createChallenge(FamilyChallenge challenge);
    FamilyChallenge updateChallenge(FamilyChallenge challenge);
    void deleteChallenge(Long challengeId);
    FamilyChallenge getChallengeById(Long challengeId);
    List<FamilyChallenge> getFamilyChallenges(Long familyId);
    List<FamilyChallenge> getActiveChallenges(Long familyId);
    
    // 挑战参与
    ChallengeParticipant joinChallenge(Long challengeId, Long userId);
    void leaveChallenge(Long challengeId, Long userId);
    List<ChallengeParticipant> getChallengeParticipants(Long challengeId);
    ChallengeParticipant getParticipant(Long challengeId, Long userId);
    
    // 打卡
    ChallengeCheckin checkin(ChallengeCheckin checkin);
    List<ChallengeCheckin> getCheckinRecords(Long challengeId, Long userId);
    Integer getConsecutiveCheckinDays(Long challengeId, Long userId);
    
    // 猜谜游戏
    GameRiddle getRandomRiddle();
    GameRiddle getRiddleById(Long id);
    boolean verifyRiddleAnswer(Long riddleId, String answer);
    List<GameRiddle> getRiddlesByCategory(String category);
    
    // 答题游戏
    GameQuiz getRandomQuiz();
    GameQuiz getQuizById(Long id);
    boolean verifyQuizAnswer(Long quizId, String answer);
    List<GameQuiz> getQuizzesByCategory(String category);
    
    // 排行榜
    List<Map<String, Object>> getChallengeRank(Long challengeId);
}
