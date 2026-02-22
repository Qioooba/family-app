
package com.family.notify.controller;
import cn.dev33.satoken.annotation.SaCheckLogin;
import com.family.common.core.PageResult;
import com.family.common.core.Result;
import com.family.notify.entity.NotificationTemplate;
import com.family.notify.service.NotificationTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 通知模板控制器
 */
@RestController
@SaCheckLogin
@RequestMapping("/notify/template")
@RequiredArgsConstructor
public class NotificationTemplateController {
    
    private final NotificationTemplateService templateService;
    
    /**
     * 创建模板
     */
    @PostMapping
    public Result<NotificationTemplate> createTemplate(@RequestBody NotificationTemplate template) {
        return Result.success(templateService.createTemplate(template));
    }
    
    /**
     * 更新模板
     */
    @PutMapping("/{id}")
    public Result<NotificationTemplate> updateTemplate(@PathVariable Long id, @RequestBody NotificationTemplate template) {
        template.setId(id);
        return Result.success(templateService.updateTemplate(template));
    }
    
    /**
     * 删除模板
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteTemplate(@PathVariable Long id) {
        templateService.deleteTemplate(id);
        return Result.success();
    }
    
    /**
     * 获取模板详情
     */
    @GetMapping("/{id}")
    public Result<NotificationTemplate> getTemplate(@PathVariable Long id) {
        return Result.success(templateService.getById(id));
    }
    
    /**
     * 根据编码获取模板
     */
    @GetMapping("/code/{code}")
    public Result<NotificationTemplate> getTemplateByCode(@PathVariable String code) {
        return Result.success(templateService.getByCode(code));
    }
    
    /**
     * 获取模板列表
     */
    @GetMapping("/list")
    public Result<PageResult<NotificationTemplate>> getTemplateList(
            @RequestParam(required = false) Integer type,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(templateService.getTemplateList(type, keyword, pageNum, pageSize));
    }
    
    /**
     * 获取所有模板
     */
    @GetMapping("/all")
    public Result<List<NotificationTemplate>> getAllTemplates() {
        return Result.success(templateService.getAllTemplates());
    }
    
    /**
     * 渲染模板
     */
    @PostMapping("/render")
    public Result<String> renderTemplate(@RequestParam String template, @RequestBody Map<String, Object> data) {
        return Result.success(templateService.renderTemplate(template, data));
    }
    
    /**
     * 初始化系统模板
     */
    @PostMapping("/init")
    public Result<Void> initSystemTemplates() {
        templateService.initSystemTemplates();
        return Result.success();
    }
}
