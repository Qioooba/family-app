
package com.family.wish.controller;
import cn.dev33.satoken.annotation.SaCheckLogin;
import com.family.common.core.Result;
import com.family.wish.dto.WishMilestoneDTO;
import com.family.wish.service.WishMilestoneService;
import com.family.wish.vo.WishMilestoneVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 心愿里程碑控制器
 */
@RestController
@SaCheckLogin
@RequestMapping("/api")
@RequiredArgsConstructor
public class WishMilestoneController {
    
    private final WishMilestoneService milestoneService;
    
    /**
     * 添加里程碑
     * POST /api/wish/{id}/milestone
     */
    @PostMapping("/wish/{id}/milestone")
    public Result<WishMilestoneVO> addMilestone(@PathVariable Long id, @RequestBody WishMilestoneDTO dto) {
        return Result.success(milestoneService.addMilestone(id, dto));
    }
    
    /**
     * 完成里程碑
     * PUT /api/milestone/{id}/complete
     */
    @PutMapping("/milestone/{id}/complete")
    public Result<Void> completeMilestone(@PathVariable Long id) {
        milestoneService.completeMilestone(id);
        return Result.success();
    }
    
    /**
     * 获取心愿的里程碑列表
     * GET /api/wish/{id}/milestones
     */
    @GetMapping("/wish/{id}/milestones")
    public Result<List<WishMilestoneVO>> getMilestones(@PathVariable Long id) {
        return Result.success(milestoneService.getMilestones(id));
    }
    
    /**
     * 删除里程碑
     * DELETE /api/milestone/{id}
     */
    @DeleteMapping("/milestone/{id}")
    public Result<Void> deleteMilestone(@PathVariable Long id) {
        milestoneService.deleteMilestone(id);
        return Result.success();
    }
}