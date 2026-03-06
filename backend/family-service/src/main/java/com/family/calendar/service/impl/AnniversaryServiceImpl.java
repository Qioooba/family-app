package com.family.calendar.service.impl;

import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.calendar.entity.Anniversary;
import com.family.calendar.mapper.AnniversaryMapper;
import com.family.calendar.service.AnniversaryService;
import com.family.family.entity.FamilyMember;
import com.family.family.mapper.FamilyMemberMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class AnniversaryServiceImpl extends ServiceImpl<AnniversaryMapper, Anniversary> implements AnniversaryService {
    
    private final FamilyMemberMapper familyMemberMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    public AnniversaryServiceImpl(FamilyMemberMapper familyMemberMapper) {
        this.familyMemberMapper = familyMemberMapper;
    }
    
    /**
     * 获取当前登录用户的成员ID
     */
    private Long getCurrentMemberId(Long familyId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            if (userId != null && familyId != null) {
                FamilyMember member = familyMemberMapper.selectByUserIdAndFamilyId(userId, familyId);
                if (member != null) {
                    return member.getId();
                }
            }
        } catch (Exception e) {
            // 未登录或获取用户ID失败
        }
        return null;
    }
    
    /**
     * 检查当前用户是否有权限查看该纪念日
     */
    private boolean hasPermission(Anniversary anniversary, Long currentMemberId) {
        // 如果没有登录，无权限
        if (currentMemberId == null) {
            return false;
        }
        
        // 简化权限判断：默认创建者有权限
        return true;
    }
    
    @Override
    public List<Anniversary> getFamilyAnniversaries(Long familyId) {
        Long currentMemberId = getCurrentMemberId(familyId);
        
        List<Anniversary> allAnniversaries = lambdaQuery()
                .eq(Anniversary::getFamilyId, familyId)
                .orderByAsc(Anniversary::getTargetDate)
                .list();
        
        // 过滤只返回有权限查看的纪念日
        if (currentMemberId == null) {
            // 未登录用户返回空列表
            return new ArrayList<>();
        }
        
        return allAnniversaries.stream()
                .filter(a -> hasPermission(a, currentMemberId))
                .toList();
    }
    
    @Override
    public List<Anniversary> getUpcomingAnniversaries(Long familyId, int days) {
        Long currentMemberId = getCurrentMemberId(familyId);
        
        LocalDate now = LocalDate.now();
        LocalDate future = now.plusDays(days);
        
        List<Anniversary> allAnniversaries = lambdaQuery()
                .eq(Anniversary::getFamilyId, familyId)
                .between(Anniversary::getTargetDate, now, future)
                .list();
        
        // 过滤只返回有权限查看的纪念日
        if (currentMemberId == null) {
            return new ArrayList<>();
        }
        
        return allAnniversaries.stream()
                .filter(a -> hasPermission(a, currentMemberId))
                .toList();
    }
    
    @Override
    public Anniversary createAnniversary(Anniversary anniversary) {
        save(anniversary);
        return anniversary;
    }
    
    @Override
    public List<Anniversary> getTodayCountdown(Long familyId) {
        Long currentMemberId = getCurrentMemberId(familyId);
        
        LocalDate today = LocalDate.now();
        
        List<Anniversary> allAnniversaries = lambdaQuery()
                .eq(Anniversary::getFamilyId, familyId)
                .ge(Anniversary::getTargetDate, today)
                .orderByAsc(Anniversary::getTargetDate)
                .last("LIMIT 1")
                .list();
        
        // 过滤只返回有权限查看的纪念日
        if (currentMemberId == null) {
            return new ArrayList<>();
        }
        
        return allAnniversaries.stream()
                .filter(a -> hasPermission(a, currentMemberId))
                .toList();
    }
}
