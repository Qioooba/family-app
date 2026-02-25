package com.family.family.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.family.family.entity.FamilyLedger;
import com.family.family.entity.LedgerBudget;
import com.family.family.entity.LedgerCategory;
import com.family.family.entity.LedgerRecord;
import com.family.family.entity.LedgerShare;
import com.family.family.mapper.FamilyLedgerMapper;
import com.family.family.mapper.LedgerBudgetMapper;
import com.family.family.mapper.LedgerCategoryMapper;
import com.family.family.mapper.LedgerRecordMapper;
import com.family.family.mapper.LedgerShareMapper;
import com.family.family.service.LedgerService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 账本服务实现
 */
@Service
public class LedgerServiceImpl implements LedgerService {

    private final FamilyLedgerMapper familyLedgerMapper;
    private final LedgerRecordMapper ledgerRecordMapper;
    private final LedgerCategoryMapper ledgerCategoryMapper;
    private final LedgerBudgetMapper ledgerBudgetMapper;
    private final LedgerShareMapper ledgerShareMapper;

    public LedgerServiceImpl(FamilyLedgerMapper familyLedgerMapper,
                            LedgerRecordMapper ledgerRecordMapper,
                            LedgerCategoryMapper ledgerCategoryMapper,
                            LedgerBudgetMapper ledgerBudgetMapper,
                            LedgerShareMapper ledgerShareMapper) {
        this.familyLedgerMapper = familyLedgerMapper;
        this.ledgerRecordMapper = ledgerRecordMapper;
        this.ledgerCategoryMapper = ledgerCategoryMapper;
        this.ledgerBudgetMapper = ledgerBudgetMapper;
        this.ledgerShareMapper = ledgerShareMapper;
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
    public LedgerCategory updateCategory(LedgerCategory category) {
        category.setUpdateTime(LocalDateTime.now());
        ledgerCategoryMapper.updateById(category);
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
    public LedgerBudget updateBudget(LedgerBudget budget) {
        budget.setUpdateTime(LocalDateTime.now());
        ledgerBudgetMapper.updateById(budget);
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

        LocalDate now = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");

        for (int i = months - 1; i >= 0; i--) {
            LocalDate monthDate = now.minusMonths(i);
            String yearMonth = monthDate.format(formatter);

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

            trend.put(yearMonth + "_income", income);
            trend.put(yearMonth + "_expense", expense);
        }

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

    @Override
    public List<LedgerShare> createShare(Long recordId, List<LedgerShare> shares) {
        LedgerRecord record = ledgerRecordMapper.selectById(recordId);
        if (record == null) {
            return new ArrayList<>();
        }

        // 删除旧的分摊记录
        QueryWrapper<LedgerShare> wrapper = new QueryWrapper<>();
        wrapper.eq("record_id", recordId);
        ledgerShareMapper.delete(wrapper);

        // 插入新的分摊记录
        for (LedgerShare share : shares) {
            share.setRecordId(recordId);
            share.setLedgerId(record.getLedgerId());
            share.setFamilyId(record.getFamilyId());
            share.setStatus(1);
            share.setCreateTime(LocalDateTime.now());
            share.setUpdateTime(LocalDateTime.now());
            ledgerShareMapper.insert(share);
        }
        return shares;
    }

    @Override
    public void deleteShare(Long shareId) {
        LedgerShare share = ledgerShareMapper.selectById(shareId);
        if (share != null) {
            share.setStatus(0);
            share.setUpdateTime(LocalDateTime.now());
            ledgerShareMapper.updateById(share);
        }
    }

    @Override
    public List<LedgerShare> getRecordShares(Long recordId) {
        QueryWrapper<LedgerShare> wrapper = new QueryWrapper<>();
        wrapper.eq("record_id", recordId)
               .eq("status", 1);
        return ledgerShareMapper.selectList(wrapper);
    }

    @Override
    public List<LedgerShare> getMemberShares(Long ledgerId, Long memberId, String startDate, String endDate) {
        QueryWrapper<LedgerShare> wrapper = new QueryWrapper<>();
        wrapper.eq("ledger_id", ledgerId)
               .eq("member_id", memberId)
               .eq("status", 1);

        if (startDate != null && endDate != null) {
            // 通过recordId关联查询日期范围
            QueryWrapper<LedgerRecord> recordWrapper = new QueryWrapper<>();
            recordWrapper.eq("ledger_id", ledgerId)
                        .ge("record_date", startDate)
                        .le("record_date", endDate)
                        .eq("status", 1);
            List<LedgerRecord> records = ledgerRecordMapper.selectList(recordWrapper);
            if (!records.isEmpty()) {
                List<Long> recordIds = records.stream().map(LedgerRecord::getId).collect(Collectors.toList());
                wrapper.in("record_id", recordIds);
            }
        }

        return ledgerShareMapper.selectList(wrapper);
    }

    @Override
    public Map<String, Object> calculateSplit(Long ledgerId, String startDate, String endDate) {
        Map<String, Object> result = new HashMap<>();

        // 获取所有相关记录
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

        // 计算每个人的支出和应付款
        Map<Long, BigDecimal> paidMap = new HashMap<>();      // 每人实际支付
        Map<Long, BigDecimal> shouldPayMap = new HashMap<>(); // 每人应该承担

        for (LedgerRecord record : records) {
            Long payerId = record.getPayerId();
            BigDecimal amount = record.getAmount();

            // 记录支付金额
            paidMap.merge(payerId, amount, BigDecimal::add);

            // 获取分摊记录
            List<LedgerShare> shares = getRecordShares(record.getId());
            if (shares.isEmpty()) {
                // 没有分摊记录，默认 payer 承担全部
                shouldPayMap.merge(payerId, amount, BigDecimal::add);
            } else {
                for (LedgerShare share : shares) {
                    shouldPayMap.merge(share.getMemberId(), share.getAmount(), BigDecimal::add);
                }
            }
        }

        // 计算每人净额（正数表示应收，负数表示应付）
        Map<Long, BigDecimal> balanceMap = new HashMap<>();
        for (Long memberId : paidMap.keySet()) {
            BigDecimal paid = paidMap.getOrDefault(memberId, BigDecimal.ZERO);
            BigDecimal shouldPay = shouldPayMap.getOrDefault(memberId, BigDecimal.ZERO);
            balanceMap.put(memberId, paid.subtract(shouldPay));
        }
        for (Long memberId : shouldPayMap.keySet()) {
            if (!balanceMap.containsKey(memberId)) {
                balanceMap.put(memberId, BigDecimal.ZERO.subtract(shouldPayMap.get(memberId)));
            }
        }

        result.put("paid", paidMap);
        result.put("shouldPay", shouldPayMap);
        result.put("balance", balanceMap);

        // 计算最优转账方案
        List<Map<String, Object>> transactions = calculateOptimalTransactions(balanceMap);
        result.put("transactions", transactions);

        return result;
    }

    private List<Map<String, Object>> calculateOptimalTransactions(Map<Long, BigDecimal> balanceMap) {
        List<Map<String, Object>> transactions = new ArrayList<>();

        // 分离应收和应付
        List<Map.Entry<Long, BigDecimal>> creditors = new ArrayList<>();  // 应收
        List<Map.Entry<Long, BigDecimal>> debtors = new ArrayList<>();    // 应付

        for (Map.Entry<Long, BigDecimal> entry : balanceMap.entrySet()) {
            if (entry.getValue().compareTo(BigDecimal.ZERO) > 0) {
                creditors.add(entry);
            } else if (entry.getValue().compareTo(BigDecimal.ZERO) < 0) {
                debtors.add(entry);
            }
        }

        // 贪心算法计算转账
        int i = 0, j = 0;
        while (i < creditors.size() && j < debtors.size()) {
            Map.Entry<Long, BigDecimal> creditor = creditors.get(i);
            Map.Entry<Long, BigDecimal> debtor = debtors.get(j);

            BigDecimal creditAmount = creditor.getValue();
            BigDecimal debtAmount = debtor.getValue().abs();
            BigDecimal transferAmount = creditAmount.min(debtAmount);

            Map<String, Object> transaction = new HashMap<>();
            transaction.put("from", debtor.getKey());      // 付款人
            transaction.put("to", creditor.getKey());      // 收款人
            transaction.put("amount", transferAmount);
            transactions.add(transaction);

            // 更新余额
            creditor.setValue(creditor.getValue().subtract(transferAmount));
            debtor.setValue(debtor.getValue().add(transferAmount));

            if (creditor.getValue().compareTo(BigDecimal.ZERO) == 0) i++;
            if (debtor.getValue().compareTo(BigDecimal.ZERO) == 0) j++;
        }

        return transactions;
    }
}
