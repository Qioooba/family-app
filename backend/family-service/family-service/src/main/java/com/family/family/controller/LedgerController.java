package com.family.family.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.family.common.core.Result;
import com.family.family.entity.FamilyLedger;
import com.family.family.entity.LedgerBudget;
import com.family.family.entity.LedgerCategory;
import com.family.family.entity.LedgerRecord;
import com.family.family.service.LedgerService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 账本控制器
 */
@RestController
@RequestMapping("/api/ledger")
public class LedgerController {
    
    private final LedgerService ledgerService;
    
    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }
    
    // ========== 账本管理 ==========
    
    @PostMapping
    public Result<FamilyLedger> createLedger(@RequestBody FamilyLedger ledger) {
        Long userId = StpUtil.getLoginIdAsLong();
        ledger.setCreatorId(userId);
        return Result.success(ledgerService.createLedger(ledger));
    }
    
    @PutMapping("/{id}")
    public Result<FamilyLedger> updateLedger(@PathVariable Long id, @RequestBody FamilyLedger ledger) {
        ledger.setId(id);
        return Result.success(ledgerService.updateLedger(ledger));
    }
    
    @DeleteMapping("/{id}")
    public Result<Void> deleteLedger(@PathVariable Long id) {
        ledgerService.deleteLedger(id);
        return Result.success();
    }
    
    @GetMapping("/{id}")
    public Result<FamilyLedger> getLedgerById(@PathVariable Long id) {
        return Result.success(ledgerService.getLedgerById(id));
    }
    
    @GetMapping("/family/{familyId}")
    public Result<List<FamilyLedger>> getFamilyLedgers(@PathVariable Long familyId) {
        return Result.success(ledgerService.getFamilyLedgers(familyId));
    }
    
    // ========== 记账 ==========
    
    @PostMapping("/record")
    public Result<LedgerRecord> addRecord(@RequestBody LedgerRecord record) {
        return Result.success(ledgerService.addRecord(record));
    }
    
    @DeleteMapping("/record/{id}")
    public Result<Void> deleteRecord(@PathVariable Long id) {
        ledgerService.deleteRecord(id);
        return Result.success();
    }
    
    @PutMapping("/record/{id}")
    public Result<LedgerRecord> updateRecord(@PathVariable Long id, @RequestBody LedgerRecord record) {
        record.setId(id);
        return Result.success(ledgerService.updateRecord(record));
    }
    
    @GetMapping("/record/{id}")
    public Result<LedgerRecord> getRecordById(@PathVariable Long id) {
        return Result.success(ledgerService.getRecordById(id));
    }
    
    @GetMapping("/{ledgerId}/records")
    public Result<List<LedgerRecord>> getLedgerRecords(
            @PathVariable Long ledgerId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(ledgerService.getLedgerRecords(ledgerId, startDate, endDate));
    }
    
    @GetMapping("/{ledgerId}/member/{memberId}/records")
    public Result<List<LedgerRecord>> getMemberRecords(
            @PathVariable Long ledgerId,
            @PathVariable Long memberId) {
        return Result.success(ledgerService.getMemberRecords(ledgerId, memberId));
    }
    
    // ========== 分类管理 ==========
    
    @PostMapping("/category")
    public Result<LedgerCategory> addCategory(@RequestBody LedgerCategory category) {
        return Result.success(ledgerService.addCategory(category));
    }
    
    @DeleteMapping("/category/{id}")
    public Result<Void> deleteCategory(@PathVariable Long id) {
        ledgerService.deleteCategory(id);
        return Result.success();
    }
    
    @GetMapping("/categories/{familyId}")
    public Result<List<LedgerCategory>> getCategories(
            @PathVariable Long familyId,
            @RequestParam(required = false) Integer type) {
        return Result.success(ledgerService.getCategories(familyId, type));
    }
    
    // ========== 预算管理 ==========
    
    @PostMapping("/budget")
    public Result<LedgerBudget> setBudget(@RequestBody LedgerBudget budget) {
        return Result.success(ledgerService.setBudget(budget));
    }
    
    @DeleteMapping("/budget/{id}")
    public Result<Void> deleteBudget(@PathVariable Long id) {
        ledgerService.deleteBudget(id);
        return Result.success();
    }
    
    @GetMapping("/{ledgerId}/budgets")
    public Result<List<LedgerBudget>> getBudgets(
            @PathVariable Long ledgerId,
            @RequestParam String budgetMonth) {
        return Result.success(ledgerService.getBudgets(ledgerId, budgetMonth));
    }
    
    // ========== 报表 ==========
    
    @GetMapping("/{ledgerId}/report/monthly")
    public Result<Map<String, Object>> getMonthlyReport(
            @PathVariable Long ledgerId,
            @RequestParam String yearMonth) {
        return Result.success(ledgerService.getMonthlyReport(ledgerId, yearMonth));
    }
    
    @GetMapping("/{ledgerId}/report/category")
    public Result<Map<String, Object>> getCategoryReport(
            @PathVariable Long ledgerId,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.success(ledgerService.getCategoryReport(ledgerId, startDate, endDate));
    }
    
    @GetMapping("/{ledgerId}/report/trend")
    public Result<Map<String, BigDecimal>> getTrendReport(
            @PathVariable Long ledgerId,
            @RequestParam(defaultValue = "6") Integer months) {
        return Result.success(ledgerService.getTrendReport(ledgerId, months));
    }
}
