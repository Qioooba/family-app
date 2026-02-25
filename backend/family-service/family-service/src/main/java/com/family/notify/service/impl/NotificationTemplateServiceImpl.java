package com.family.notify.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.common.core.ErrorCode;
import com.family.common.core.PageResult;
import com.family.common.core.exception.BusinessException;
import com.family.notify.entity.NotificationTemplate;
import com.family.notify.mapper.NotificationTemplateMapper;
import com.family.notify.service.NotificationTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通知模板服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationTemplateServiceImpl extends ServiceImpl<NotificationTemplateMapper, NotificationTemplate> implements NotificationTemplateService {
    
    // 模板变量正则：${variable} 或 #{variable}
    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("\\$\\{(\\w+)\\}|#\\{(\\w+)\\}");
    
    @Override
    public NotificationTemplate getByCode(String code) {
        return baseMapper.selectByCode(code);
    }
    
    @Override
    public String renderTemplate(String template, Map<String, Object> data) {
        if (StrUtil.isBlank(template) || data == null || data.isEmpty()) {
            return template;
        }
        
        Matcher matcher = TEMPLATE_PATTERN.matcher(template);
        StringBuffer sb = new StringBuffer();
        
        while (matcher.find()) {
            String key = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            Object value = data.get(key);
            if (value != null) {
                matcher.appendReplacement(sb, Matcher.quoteReplacement(value.toString()));
            } else {
                matcher.appendReplacement(sb, "");
            }
        }
        matcher.appendTail(sb);
        
        return sb.toString();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public NotificationTemplate createTemplate(NotificationTemplate template) {
        // 检查编码是否重复
        if (getByCode(template.getCode()) != null) {
            throw new BusinessException("模板编码已存在: " + template.getCode());
        }
        
        save(template);
        return template;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public NotificationTemplate updateTemplate(NotificationTemplate template) {
        NotificationTemplate exist = getById(template.getId());
        if (exist == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        
        // 系统模板不能修改
        if (exist.getIsSystem() != null && exist.getIsSystem() == 1) {
            throw new BusinessException("系统模板不能修改");
        }
        
        // 如果修改了编码，检查是否重复
        if (!exist.getCode().equals(template.getCode())) {
            if (getByCode(template.getCode()) != null) {
                throw new BusinessException("模板编码已存在: " + template.getCode());
            }
        }
        
        updateById(template);
        return template;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplate(Long templateId) {
        NotificationTemplate template = getById(templateId);
        if (template == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND);
        }
        
        // 系统模板不能删除
        if (template.getIsSystem() != null && template.getIsSystem() == 1) {
            throw new BusinessException("系统模板不能删除");
        }
        
        removeById(templateId);
    }
    
    @Override
    public PageResult<NotificationTemplate> getTemplateList(Integer type, String keyword, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<NotificationTemplate> wrapper = new LambdaQueryWrapper<>();
        
        if (type != null) {
            wrapper.eq(NotificationTemplate::getType, type);
        }
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(NotificationTemplate::getCode, keyword)
                    .or()
                    .like(NotificationTemplate::getName, keyword));
        }
        
        wrapper.orderByDesc(NotificationTemplate::getCreateTime);
        
        Page<NotificationTemplate> page = page(new Page<>(pageNum, pageSize), wrapper);
        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }
    
    @Override
    public List<NotificationTemplate> getAllTemplates() {
        LambdaQueryWrapper<NotificationTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(NotificationTemplate::getCreateTime);
        return list(wrapper);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void initSystemTemplates() {
        // 系统通知模板
        createSystemTemplate("WELCOME", "欢迎通知", 1, "1,2,4",
                "欢迎加入家庭助手",
                "亲爱的${nickname}，欢迎加入家庭助手！开始您的智慧家庭生活吧。",
                "<html><body><h2>欢迎加入家庭助手</h2><p>亲爱的${nickname}，欢迎加入家庭助手！</p><p>开始您的智慧家庭生活吧。</p></body></html>",
                "【家庭助手】欢迎加入！开始您的智慧家庭生活吧。");
        
        // 任务通知模板
        createSystemTemplate("TASK_ASSIGNED", "任务分配通知", 5, "1,4",
                "您有新的任务",
                "${assigner}给您分配了新任务：${taskName}，截止时间：${deadline}",
                null,
                null);
        
        // 提醒模板
        createSystemTemplate("REMINDER", "提醒通知", 3, "1,3,4",
                "${reminderTitle}",
                "${reminderContent}",
                null,
                "【家庭助手提醒】${reminderTitle}");
        
        // 活动通知模板
        createSystemTemplate("ACTIVITY_INVITE", "活动邀请", 4, "1,4",
                "活动邀请：${activityName}",
                "${inviter}邀请您参加${activityName}，时间：${activityTime}，地点：${activityLocation}",
                null,
                null);
        
        // 家庭公告模板
        createSystemTemplate("ANNOUNCEMENT", "家庭公告", 2, "1,4",
                "${title}",
                "${content}",
                null,
                null);
    }
    
    /**
     * 创建系统模板
     */
    private void createSystemTemplate(String code, String name, Integer type, String channels,
                                      String title, String content, String emailContent, String smsContent) {
        if (getByCode(code) != null) {
            return; // 已存在则跳过
        }
        
        NotificationTemplate template = new NotificationTemplate();
        template.setCode(code);
        template.setName(name);
        template.setType(type);
        template.setChannels(channels);
        template.setTitle(title);
        template.setContent(content);
        template.setEmailContent(emailContent);
        template.setSmsContent(smsContent);
        template.setIsSystem(1);
        template.setCreateUserId(0L);
        
        save(template);
        log.info("初始化系统模板: {}", code);
    }
}
