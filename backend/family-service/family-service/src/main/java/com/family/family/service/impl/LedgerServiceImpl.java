package com.family.family.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.family.family.entity.FamilyLedger;
import com.family.family.entity.LedgerBudget;
import com.family.family.entity.LedgerCategory;
import com.family.family.entity.LedgerRecord;
import com.family.family.mapper.FamilyLedgerMapper;
import com.family.family.mapper.LedgerBudgetMapper;
import com.family.family.mapper.LedgerCategoryMapper;
import com.family.family.mapper.LedgerRecordMapper;
import com.family.family.service.LedgerService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 账本服务实现
 */
@Service
public class LedgerServiceImpl implements LedgerService {
    
    private final FamilyLedgerMapper familyLedgerMapper;
    private final LedgerRecordMapper ledgerRecordMapper;
    private final LedgerCategoryMapper ledgerCategoryMapper;
    private final LedgerBudgetMapper ledgerBudgetMapper;
    
    public LedgerServiceImpl(FamilyLedgerMapper familyLedgerMapper,
                            LedgerRecordMapper ledgerRecordMapper,
                            LedgerCategoryMapper ledgerCategoryMapper,
                            LedgerBudgetMapper ledgerBudgetMapper) {
        this.familyLedgerMapper = familyLedgerMapper;
        this.ledgerRecordMapper = ledgerRecordMapper;
        this.ledgerCategoryMapper = ledgerCategoryMapper;
        this.ledgerBudgetMapper = ledgerBudgetMapper;
    }
    
    @Override
    public FamilyLedger createLedger(FamilyLedger ledger) {
        ledger.setTotalIncome(BigDecimal.ZERO);
        ledger.setTotalExpense(BigDecimal.ZERO);
        ledger.setBalance(BigDecimal.ZERO);
        ledger.setStatus(1);
        ledger.setCreateTime(LocalDateTime.now());
        ledger.setUpdateTime(LocalDateTime.now());
        familyLedgerMapper.insert(ledger);
        return ledger;
    }
    
    @Override
    public FamilyLedger updateLedger(FamilyLedger ledger) {
        ledger.setUpdateTime(LocalDateTime.now());
        familyLedgerMapper.updateById(ledger);
        return ledger;
    }
    
    @Override
    public void deleteLedger(Long ledgerId) {
        FamilyLedger ledger = familyLedgerMapper.selectById(ledgerId);
        if (ledger != null) {
            ledger.setStatus(0);
            ledger.setUpdateTime(LocalDateTime.now());
            familyLedgerMapper.updateById(ledger);
        }
    }
    
    @Override
    public FamilyLedger getLedgerById(Long ledgerId) {
        return familyLedgerMapper.selectById(ledgerId);
    }
    
    @Override
    public List<FamilyLedger> getFamilyLedgers(Long familyId) {
        QueryWrapper<FamilyLedger> wrapper = new QueryWrapper<>();
        wrapper.eq("family_id", familyId)
               .eq("status", 1);
        return familyLedgerMapper.selectList(wrapper);
    }
    
    @Override
    public LedgerRecord addRecord(LedgerRecord record) {
        record.setStatus(1);
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        ledgerRecordMapper.insert(record);
        
        // 更新账本统计
        updateLedgerStats(record.getLedgerId());
        
        // 更新预算使用情况
        if (record.getType() == 2 && record.getCategoryId() != null) {
            updateBudgetByRecord(record);
        }
        
        return record;
    }
    
    @Override
    public void deleteRecord(Long recordId) {
        LedgerRecord record = ledgerRecordMapper.selectById(recordId);
        if (record != null) {
            record.setStatus(0);
            record.setUpdateTime(LocalDateTime.now());
            ledgerRecordMapper.updateById(record);
            updateLedgerStats(record.getLedgerId());
        }
    }
    
    @Override
    public LedgerRecord updateRecord(LedgerRecord record) {
        record.setUpdateTime(LocalDateTime.now());
        ledgerRecordMapper.updateById(record);
        updateLedgerStats(record.getLedgerId());
        return record;
    }
    
    @Override
    public LedgerRecord getRecordById(Long recordId) {
        return ledgerRecordMapper.selectById(recordId);
    }
    
    @Override
    public List<LedgerRecord> getLedgerRecords(Long ledgerId, String startDate, String endDate) {
        QueryWrapper<LedgerRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("ledger_id", ledgerId)
               .eq("status", 1);
        if (startDate != null) {
            wrapper.ge("record_date", startDate);
        }
        if (endDate != null) {
            wrapper.le("record_date", endDate);
        }
        wrapper.orderByDesc("record_date");
        return ledgerRecordMapper.selectList(wrapper);
    }
    
    @Override
    public List<LedgerRecord> getMemberRecords(Long ledgerId, Long memberId) {
        QueryWrapper<LedgerRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("ledger_id", ledgerId)
               .eq("payer_id", memberId)
               .eq("status", 1)
               .orderByDesc("record_date");
        return ledgerRecordMapper.selectList(wrapper);
    }
    
    @Override
    public LedgerCategory addCategory(LedgerCategory category) {
        category.setStatus(1);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        ledgerCategoryMapper.insert(category);
        return category;
    }
    
    @Override
    public void deleteCategory(Long categoryId) {
        LedgerCategory category = ledgerCategoryMapper.selectById(categoryId);
        if (category != null) {
            category.setStatus(0);
            category.setUpdateTime(LocalDateTime.now());
            ledgerCategoryMapper.updateById(category);
        }
    }
    
    @Override
    public List<LedgerCategory> getCategories(Long familyId, Integer type) {
        QueryWrapper<LedgerCategory> wrapper = new QueryWrapper<>();
        wrapper.eq("family_id", familyId)
               .eq("status", 1);
        if (type != null) {
            wrapper.eq("type", type);
        }
        wrapper.orderByAsc("sort_order");
        return ledgerCategoryMapper.selectList(wrapper);
    }
    
    @Override
    public LedgerBudget setBudget(LedgerBudget budget) {
        budget.setUsedAmount(BigDecimal.ZERO);
        budget.setStatus(1);
        budget.setCreateTime(LocalDateTime.now());
        budget.setUpdateTime(LocalDateTime.now());
        ledgerBudgetMapper.insert(budget);
        return budget;
    }
    
    @Override
    public void deleteBudget(Long budgetId) {
        LedgerBudget budget = ledgerBudgetMapper.selectById(budgetId);
        if (budget != null) {
            budget.setStatus(0);
            budget.setUpdateTime(LocalDateTime.now());
            ledgerBudgetMapper.updateById(budget);
        }
    }
    
    @Override
    public List<LedgerBudget> getBudgets(Long ledgerId, String budgetMonth) {
        QueryWrapper<LedgerBudget> wrapper = new QueryWrapper<>();
        wrapper.eq("ledger_id", ledgerId)
               .eq("budget_month", budgetMonth)
               .eq("status", 1);
        return ledgerBudgetMapper.selectList(wrapper);
    }
    
    @Override
    public LedgerBudget getBudgetByCategory(Long ledgerId, Long categoryId, String budgetMonth) {
        QueryWrapper<LedgerBudget> wrapper = new QueryWrapper<>();
        wrapper.eq("ledger_id", ledgerId)
               .eq("category_id", categoryId)
               .eq("budget_month", budgetMonth)
               .eq("status", 1);
        return ledgerBudgetMapper.selectOne(wrapper);
    }
    
    @Override
    public void updateBudgetUsed(Long budgetId, BigDecimal amount) {
        LedgerBudget budget = ledgerBudgetMapper.selectById(budgetId);
        if (budget != null) {
            budget.setUsedAmount(budget.getUsedAmount().add(amount));
            budget.setUpdateTime(LocalDateTime.now());
            ledgerBudgetMapper.updateById(budget);
        }
    }
    
    @Override
    public Map<String, Object> getMonthlyReport(Long ledgerId, String yearMonth) {
        Map<String, Object> report = new HashMap<>();
        
        // 计算月度收支
        QueryWrapper<LedgerRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("ledger_id", ledgerId)
               .likeRight("record_date", yearMonth)
               .eq("status", 1);
        List<LedgerRecord> records = ledgerRecordMapper.selectList(wrapper);
        
        BigDecimal income = records.stream()
                .filter(r -> r.getType() == 1)
                .map(LedgerRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal expense = records.stream()
                .filter(r -> r.getType() == 2)
                .map(LedgerRecord::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        report.put("yearMonth", yearMonth);
        report.put("income", income);
        report.put("expense", expense);
        report.put("balance", income.subtract(expense));
        report.put("recordCount", records.size());
        
        return report;
    }
    
    @Override
    public Map<String, Object> getCategoryReport(Long ledgerId, String startDate, String endDate) {
        Map<String, Object> report = new HashMap<>();
        
        QueryWrapper<LedgerRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("ledger_id", ledgerId)
               .eq("status", 1);
        if (startDate != null) {
            wrapper.ge("record_date", startDate);
        }
        if (endDate != null) {
            wrapper.le("record_date", endDate);
        }
        List<LedgerRecord> records = ledgerRecordMapper.selectList(wrapper);
        
        Map<Long, BigDecimal> categoryExpense = new HashMap<>();
        for (LedgerRecord record : records) {
            if (record.getType() == 2) {
                categoryExpense.merge(record.getCategoryId(), record.getAmount(), BigDecimal::add);
            }
        }
        
        report.put("categoryExpense", categoryExpense);
        report.put("totalExpense", categoryExpense.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add));
        
        return report;
    }
    
    @Override
    public Map<String, BigDecimal> getTrendReport(Long ledgerId, Integer months) {
        Map<String, BigDecimal> trend = new HashMap<>();
        // 简化的趋势分析，返回最近几个月的支出
        return trend;
    }
    
    private void updateLedgerStats(Long ledgerId) {
        FamilyLedger ledger = familyLedgerMapper.selectById(ledgerId);
        if (ledger != null) {
            QueryWrapper<LedgerRecord> wrapper = new QueryWrapper<>();
            wrapper.eq("ledger_id", ledgerId)
                   .eq("status", 1);
            List<LedgerRecord> records = ledgerRecordMapper.selectList(wrapper);
            
            BigDecimal income = records.stream()
                    .filter(r -> r.getType() == 1)
                    .map(LedgerRecord::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            BigDecimal expense = records.stream()
                    .filter(r -> r.getType() == 2)
                    .map(LedgerRecord::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            ledger.setTotalIncome(income);
            ledger.setTotalExpense(expense);
            ledger.setBalance(income.subtract(expense));
            ledger.setUpdateTime(LocalDateTime.now());
            familyLedgerMapper.updateById(ledger);
        }
    }
    
    private void updateBudgetByRecord(LedgerRecord record) {
        String yearMonth = record.getRecordDate().toString().substring(0, 7);
        LedgerBudget budget = getBudgetByCategory(record.getLedgerId(), record.getCategoryId(), yearMonth);
        if (budget != null) {
            updateBudgetUsed(budget.getId(), record.getAmount());
        }
    }
}
