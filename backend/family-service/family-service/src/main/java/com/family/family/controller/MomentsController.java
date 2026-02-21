package com.family.family.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.family.common.core.PageResult;
import com.family.common.core.Result;
import com.family.family.entity.Moment;
import com.family.family.entity.MomentComment;
import com.family.family.service.MomentService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 家庭动态控制器
 */
@RestController
@RequestMapping("/api/moments")
@SaCheckLogin
public class MomentsController {

    private final MomentService momentService;

    public MomentsController(MomentService momentService) {
        this.momentService = momentService;
    }

    /**
     * 获取动态列表
     * GET /api/moments/list
     */
    @GetMapping("/list")
    public Result<PageResult<Moment>> list(
            @RequestParam Long familyId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(momentService.list(familyId, userId, page, size));
    }

    /**
     * 创建动态
     * POST /api/moments/create
     */
    @PostMapping("/create")
    public Result<Moment> create(@RequestBody Moment moment) {
        Long userId = StpUtil.getLoginIdAsLong();
        moment.setUserId(userId);
        return Result.success(momentService.create(moment, userId));
    }

    /**
     * 点赞动态
     * POST /api/moments/like
     */
    @PostMapping("/like")
    public Result<Void> like(@RequestParam Long momentId) {
        Long userId = StpUtil.getLoginIdAsLong();
        momentService.like(momentId, userId);
        return Result.success();
    }

    /**
     * 评论动态
     * POST /api/moments/comment
     */
    @PostMapping("/comment")
    public Result<MomentComment> comment(@RequestBody MomentComment comment) {
        Long userId = StpUtil.getLoginIdAsLong();
        comment.setUserId(userId);
        return Result.success(momentService.comment(comment, userId));
    }

    /**
     * 获取动态详情
     * GET /api/moments/detail
     */
    @GetMapping("/detail")
    public Result<Moment> detail(@RequestParam Long momentId) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(momentService.detail(momentId, userId));
    }

    /**
     * 删除动态
     * POST /api/moments/delete
     */
    @PostMapping("/delete")
    public Result<Void> delete(@RequestParam Long momentId) {
        Long userId = StpUtil.getLoginIdAsLong();
        momentService.delete(momentId, userId);
        return Result.success();
    }
}
