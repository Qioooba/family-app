package com.family.notify.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.family.common.core.PageResult;
import com.family.notify.entity.NotificationTemplate;

import java.util.List;
import java.util.Map;

/**
 * 通知模板服务接口
 */
public interface NotificationTemplateService extends IService<NotificationTemplate> {
    
    /**
     * 根据编码获取模板
     */
    NotificationTemplate getByCode(String code);
    
    /**
     * 渲染模板内容
     */
    String renderTemplate(String template, Map<String, Object> data);
    
    /**
     * 创建模板
     */
    NotificationTemplate createTemplate(NotificationTemplate template);
    
    /**
     * 更新模板
     */
    NotificationTemplate updateTemplate(NotificationTemplate template);
    
    /**
     * 删除模板
     */
    void deleteTemplate(Long templateId);
    
    /**
     * 获取模板列表
     */
    PageResult<NotificationTemplate> getTemplateList(Integer type, String keyword, Integer pageNum, Integer pageSize);
    
    /**
     * 获取所有可用模板
     */
    List<NotificationTemplate> getAllTemplates();
    
    /**
     * 初始化系统默认模板
     */
    void initSystemTemplates();
}
