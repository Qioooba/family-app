
package com.family.notify.controller;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;import com.family.common.core.PageResult;
import com.family.common.core.Result;
import com.family.notify.dto.SendNotificationDTO;
import com.family.family.entity.Notification;
import com.family.notify.service.NotificationService;
import com.family.notify.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

/**
 * 家庭公告控制器
 */
@RestController
@SaCheckLogin
@RequestMapping("/notify/announcement")
@RequiredArgsConstructor
public class AnnouncementController {
    
    private final NotificationService notificationService;
    
    /**
     * 发布公告
     */
    @PostMapping
    public Result<Void> publishAnnouncement(@RequestBody AnnouncementDTO dto) {
        Long senderId = StpUtil.getLoginIdAsLong();
        
        SendNotificationDTO sendDTO = new SendNotificationDTO();
        sendDTO.setUserIds(dto.getTargetUserIds());
        sendDTO.setType(2); // 家庭公告
        sendDTO.setChannels(dto.getChannels() != null ? dto.getChannels() : "1,4");
        sendDTO.setTitle(dto.getTitle());
        sendDTO.setContent(dto.getContent());
        sendDTO.setFamilyId(dto.getFamilyId());
        sendDTO.setBizType("announcement");
        
        notificationService.sendNotification(sendDTO);
        return Result.success();
    }
    
    /**
     * 获取家庭公告列表
     */
    @GetMapping("/list")
    public Result<PageResult<NotificationVO>> getAnnouncementList(
            @RequestParam(required = false) Long familyId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 构建查询条件
        com.family.notify.dto.NotificationQueryDTO queryDTO = new com.family.notify.dto.NotificationQueryDTO();
        queryDTO.setUserId(userId);
        queryDTO.setType(2); // 家庭公告
        queryDTO.setPageNum(pageNum);
        queryDTO.setPageSize(pageSize);
        
        return Result.success(notificationService.getNotificationList(queryDTO));
    }
    
    /**
     * 获取公告详情
     */
    @GetMapping("/{id}")
    public Result<NotificationVO> getAnnouncement(@PathVariable Long id) {
        return Result.success(notificationService.getNotificationDetail(id));
    }
    
    /**
     * 删除公告
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteAnnouncement(@PathVariable Long id) {
        notificationService.deleteNotification(id);
        return Result.success();
    }
    
    /**
     * 公告DTO
     */
    @lombok.Data
    public static class AnnouncementDTO {
        /**
         * 目标用户ID列表
         */
        private java.util.List<Long> targetUserIds;
        
        /**
         * 公告标题
         */
        private String title;
        
        /**
         * 公告内容
         */
        private String content;
        
        /**
         * 家庭ID
         */
        private Long familyId;
        
        /**
         * 发送渠道
         */
        private String channels;
    }
}
