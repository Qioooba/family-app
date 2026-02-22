package com.family.family.service;

import com.family.family.entity.FamilyLedger;
import com.family.family.entity.LedgerBudget;
import com.family.family.entity.LedgerCategory;
import com.family.family.entity.LedgerRecord;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 账本服务
 */
public interface LedgerService {
    
    // 账本管理
    FamilyLedger createLedger(FamilyLedger ledger);
    FamilyLedger updateLedger(FamilyLedger ledger);
    void deleteLedger(Long ledgerId);
    FamilyLedger getLedgerById(Long ledgerId);
    List<FamilyLedger> getFamilyLedgers(Long familyId);
    
    // 记账
    LedgerRecord addRecord(LedgerRecord record);
    void deleteRecord(Long recordId);
    LedgerRecord updateRecord(LedgerRecord record);
    LedgerRecord getRecordById(Long recordId);
    List<LedgerRecord> getLedgerRecords(Long ledgerId, String startDate, String endDate);
    List<LedgerRecord> getMemberRecords(Long ledgerId, Long memberId);
    
    // 分类管理
    LedgerCategory addCategory(LedgerCategory category);
    void deleteCategory(Long categoryId);
    List<LedgerCategory> getCategories(Long familyId, Integer type);
    
    // 预算管理
    LedgerBudget setBudget(LedgerBudget budget);
    void deleteBudget(Long budgetId);
    List<LedgerBudget> getBudgets(Long ledgerId, String budgetMonth);
    LedgerBudget getBudgetByCategory(Long ledgerId, Long categoryId, String budgetMonth);
    void updateBudgetUsed(Long budgetId, BigDecimal amount);
    
    // 报表
    Map<String, Object> getMonthlyReport(Long ledgerId, String yearMonth);
    Map<String, Object> getCategoryReport(Long ledgerId, String startDate, String endDate);
    Map<String, BigDecimal> getTrendReport(Long ledgerId, Integer months);
}
