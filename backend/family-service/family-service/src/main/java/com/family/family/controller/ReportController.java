
package com.family.family.controller;
import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.hutool.json.JSONUtil;
import com.family.common.core.Result;
import com.family.family.entity.FamilyReport;
import com.family.family.mapper.FamilyReportMapper;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 家庭报告控制器
 */
@RestController
@SaCheckLogin
@RequestMapping("/api/report")
public class ReportController {
    
    private final FamilyReportMapper reportMapper;
    
    public ReportController(FamilyReportMapper reportMapper) {
        this.reportMapper = reportMapper;
    }
    
    /**
     * 生成报告
     * POST /api/report/generate
     */
    @PostMapping("/generate")
    public Result<FamilyReport> generate(@RequestBody GenerateRequest request) {
        // 生成报告内容
        Map<String, Object> content = new HashMap<>();
        
        if ("weekly".equals(request.getReportType())) {
            content.put("summary", "本周家庭活动丰富多彩");
            content.put("taskCompleted", 15);
            content.put("taskTotal", 20);
            content.put("wishProgress", "3个心愿有新进展");
        } else if ("monthly".equals(request.getReportType())) {
            content.put("summary", "本月家庭和谐度良好");
            content.put("highlights", List.of("完成了家庭旅行计划", "新增5个菜谱"));
        }
        
        FamilyReport report = new FamilyReport();
        report.setFamilyId(request.getFamilyId());
        report.setReportType(request.getReportType());
        report.setReportDate(LocalDate.now());
        report.setTitle(generateTitle(request.getReportType()));
        report.setContent(JSONUtil.toJsonStr(content));
        report.setScore(85); // 默认评分
        report.setIsRead(0);
        
        reportMapper.insert(report);
        
        return Result.success(report);
    }
    
    /**
     * 获取报告详情
     * GET /api/report/{id}
     */
    @GetMapping("/{id}")
    public Result<FamilyReport> getById(@PathVariable Long id) {
        FamilyReport report = reportMapper.selectById(id);
        if (report != null) {
            report.setIsRead(1);
            reportMapper.updateById(report);
        }
        return Result.success(report);
    }
    
    /**
     * 获取报告列表
     * GET /api/reports
     */
    @GetMapping("s")
    public Result<List<FamilyReport>> getList(@RequestParam Long familyId) {
        return Result.success(reportMapper.selectByFamilyId(familyId));
    }
    
    private String generateTitle(String reportType) {
        LocalDate now = LocalDate.now();
        return switch (reportType) {
            case "weekly" -> now + " 周报";
            case "monthly" -> now.getYear() + "年" + now.getMonthValue() + "月 月报";
            case "yearly" -> now.getYear() + "年 年报";
            default -> "家庭报告";
        };
    }
    
    public static class GenerateRequest {
        private Long familyId;
        private String reportType; // weekly/monthly/yearly
        
        public Long getFamilyId() { return familyId; }
        public void setFamilyId(Long familyId) { this.familyId = familyId; }
        
        public String getReportType() { return reportType; }
        public void setReportType(String reportType) { this.reportType = reportType; }
    }
}