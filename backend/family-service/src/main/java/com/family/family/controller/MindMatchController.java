package com.family.family.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.family.common.core.Result;
import com.family.family.entity.MindMatchQuestion;
import com.family.family.entity.MindMatchSession;
import com.family.family.service.MindMatchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 默契问答控制器
 */
@RestController
@RequestMapping("/api/game/mindmatch")
public class MindMatchController {
    
    @Autowired
    private MindMatchService mindMatchService;
    
    /**
     * 初始化问题库
     */
    @PostMapping("/init")
    public Result<Void> initQuestions() {
        mindMatchService.initQuestions();
        return Result.success();
    }
    
    /**
     * 获取随机问题
     */
    @GetMapping("/question")
    public Result<MindMatchQuestion> getRandomQuestion(@RequestParam(required = false) String type) {
        return Result.success(mindMatchService.getRandomQuestion(type));
    }
    
    /**
     * 创建游戏会话
     */
    @PostMapping("/session")
    public Result<MindMatchSession> createSession(@RequestBody Map<String, Long> params) {
        Long familyId = params.get("familyId");
        Long player2Id = params.get("player2Id");
        
        if (familyId == null || player2Id == null) {
            return Result.error(400, "参数不完整");
        }
        
        Long player1Id = StpUtil.getLoginIdAsLong();
        MindMatchSession session = mindMatchService.createSession(familyId, player1Id, player2Id);
        return Result.success(session);
    }
    
    /**
     * 回答问题
     */
    @PostMapping("/session/{sessionId}/answer")
    public Result<MindMatchSession> answerQuestion(@PathVariable Long sessionId, @RequestBody Map<String, String> params) {
        Long userId = StpUtil.getLoginIdAsLong();
        String answer = params.get("answer");
        
        if (answer == null || answer.trim().isEmpty()) {
            return Result.error(400, "答案不能为空");
        }
        
        MindMatchSession session = mindMatchService.answerQuestion(sessionId, userId, answer);
        if (session == null) {
            return Result.error(404, "会话不存在");
        }
        
        return Result.success(session);
    }
    
    /**
     * 获取会话详情
     */
    @GetMapping("/session/{sessionId}")
    public Result<MindMatchSession> getSession(@PathVariable Long sessionId) {
        MindMatchSession session = mindMatchService.getSessionById(sessionId);
        if (session == null) {
            return Result.error(404, "会话不存在");
        }
        
        // 隐藏对方答案
        Long currentUserId = StpUtil.getLoginIdAsLong();
        if (!currentUserId.equals(session.getPlayer1Id()) && !currentUserId.equals(session.getPlayer2Id())) {
            // 非参与者，隐藏所有答案
            session.setPlayer1Answer(null);
            session.setPlayer2Answer(null);
        } else if (currentUserId.equals(session.getPlayer1Id())) {
            // 玩家1只能看到自己的答案
            session.setPlayer2Answer(null);
        } else {
            // 玩家2只能看到自己的答案
            session.setPlayer1Answer(null);
        }
        
        return Result.success(session);
    }
    
    /**
     * 获取家庭游戏历史
     */
    @GetMapping("/history/{familyId}")
    public Result<List<Map<String, Object>>> getFamilyHistory(@PathVariable Long familyId) {
        return Result.success(mindMatchService.getFamilyGameHistory(familyId));
    }
    
    /**
     * 获取我的游戏统计
     */
    @GetMapping("/stats")
    public Result<Map<String, Object>> getMyStats() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(mindMatchService.getUserGameStats(userId));
    }
    
    /**
     * 获取家庭默契值
     */
    @GetMapping("/match-rate/{familyId}")
    public Result<Map<String, Object>> getMatchRate(@PathVariable Long familyId) {
        return Result.success(mindMatchService.getMatchRate(familyId));
    }
}
