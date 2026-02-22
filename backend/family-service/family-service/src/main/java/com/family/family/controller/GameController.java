package com.family.family.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.family.common.core.Result;
import com.family.family.entity.ChallengeCheckin;
import com.family.family.entity.ChallengeParticipant;
import com.family.family.entity.FamilyChallenge;
import com.family.family.entity.GameQuiz;
import com.family.family.entity.GameRiddle;
import com.family.family.service.GameService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 游戏控制器
 */
@RestController
@RequestMapping("/api/game")
public class GameController {
    
    private final GameService gameService;
    
    public GameController(GameService gameService) {
        this.gameService = gameService;
    }
    
    // ========== 家庭挑战 ==========
    
    @PostMapping("/challenge")
    public Result<FamilyChallenge> createChallenge(@RequestBody FamilyChallenge challenge) {
        Long userId = StpUtil.getLoginIdAsLong();
        challenge.setCreatorId(userId);
        return Result.success(gameService.createChallenge(challenge));
    }
    
    @PutMapping("/challenge/{id}")
    public Result<FamilyChallenge> updateChallenge(@PathVariable Long id, @RequestBody FamilyChallenge challenge) {
        challenge.setId(id);
        return Result.success(gameService.updateChallenge(challenge));
    }
    
    @DeleteMapping("/challenge/{id}")
    public Result<Void> deleteChallenge(@PathVariable Long id) {
        gameService.deleteChallenge(id);
        return Result.success();
    }
    
    @GetMapping("/challenge/{id}")
    public Result<FamilyChallenge> getChallengeById(@PathVariable Long id) {
        return Result.success(gameService.getChallengeById(id));
    }
    
    @GetMapping("/challenges/family/{familyId}")
    public Result<List<FamilyChallenge>> getFamilyChallenges(@PathVariable Long familyId) {
        return Result.success(gameService.getFamilyChallenges(familyId));
    }
    
    @GetMapping("/challenges/active/{familyId}")
    public Result<List<FamilyChallenge>> getActiveChallenges(@PathVariable Long familyId) {
        return Result.success(gameService.getActiveChallenges(familyId));
    }
    
    // ========== 挑战参与 ==========
    
    @PostMapping("/challenge/{challengeId}/join")
    public Result<ChallengeParticipant> joinChallenge(@PathVariable Long challengeId) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(gameService.joinChallenge(challengeId, userId));
    }
    
    @PostMapping("/challenge/{challengeId}/leave")
    public Result<Void> leaveChallenge(@PathVariable Long challengeId) {
        Long userId = StpUtil.getLoginIdAsLong();
        gameService.leaveChallenge(challengeId, userId);
        return Result.success();
    }
    
    @GetMapping("/challenge/{challengeId}/participants")
    public Result<List<ChallengeParticipant>> getChallengeParticipants(@PathVariable Long challengeId) {
        return Result.success(gameService.getChallengeParticipants(challengeId));
    }
    
    @GetMapping("/challenge/{challengeId}/participant")
    public Result<ChallengeParticipant> getParticipant(@PathVariable Long challengeId) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(gameService.getParticipant(challengeId, userId));
    }
    
    // ========== 打卡 ==========
    
    @PostMapping("/challenge/checkin")
    public Result<ChallengeCheckin> checkin(@RequestBody ChallengeCheckin checkin) {
        Long userId = StpUtil.getLoginIdAsLong();
        checkin.setUserId(userId);
        return Result.success(gameService.checkin(checkin));
    }
    
    @GetMapping("/challenge/{challengeId}/checkins")
    public Result<List<ChallengeCheckin>> getCheckinRecords(@PathVariable Long challengeId) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(gameService.getCheckinRecords(challengeId, userId));
    }
    
    @GetMapping("/challenge/{challengeId}/consecutive-days")
    public Result<Integer> getConsecutiveCheckinDays(@PathVariable Long challengeId) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(gameService.getConsecutiveCheckinDays(challengeId, userId));
    }
    
    @GetMapping("/challenge/{challengeId}/rank")
    public Result<List<Map<String, Object>>> getChallengeRank(@PathVariable Long challengeId) {
        return Result.success(gameService.getChallengeRank(challengeId));
    }
    
    // ========== 猜谜游戏 ==========
    
    @GetMapping("/riddle/random")
    public Result<GameRiddle> getRandomRiddle() {
        return Result.success(gameService.getRandomRiddle());
    }
    
    @GetMapping("/riddle/{id}")
    public Result<GameRiddle> getRiddleById(@PathVariable Long id) {
        return Result.success(gameService.getRiddleById(id));
    }
    
    @PostMapping("/riddle/{id}/verify")
    public Result<Boolean> verifyRiddleAnswer(@PathVariable Long id, @RequestParam String answer) {
        return Result.success(gameService.verifyRiddleAnswer(id, answer));
    }
    
    @GetMapping("/riddles/category/{category}")
    public Result<List<GameRiddle>> getRiddlesByCategory(@PathVariable String category) {
        return Result.success(gameService.getRiddlesByCategory(category));
    }
    
    // ========== 答题游戏 ==========
    
    @GetMapping("/quiz/random")
    public Result<GameQuiz> getRandomQuiz() {
        return Result.success(gameService.getRandomQuiz());
    }
    
    @GetMapping("/quiz/{id}")
    public Result<GameQuiz> getQuizById(@PathVariable Long id) {
        return Result.success(gameService.getQuizById(id));
    }
    
    @PostMapping("/quiz/{id}/verify")
    public Result<Boolean> verifyQuizAnswer(@PathVariable Long id, @RequestParam String answer) {
        return Result.success(gameService.verifyQuizAnswer(id, answer));
    }
    
    @GetMapping("/quizzes/category/{category}")
    public Result<List<GameQuiz>> getQuizzesByCategory(@PathVariable String category) {
        return Result.success(gameService.getQuizzesByCategory(category));
    }
}
