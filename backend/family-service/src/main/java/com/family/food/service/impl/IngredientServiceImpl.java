package com.family.food.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.food.entity.Ingredient;
import com.family.food.mapper.IngredientMapper;
import com.family.food.service.IngredientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class IngredientServiceImpl extends ServiceImpl<IngredientMapper, Ingredient> implements IngredientService {

    @Override
    public List<Ingredient> getFamilyIngredients(Long familyId) {
        try {
            return lambdaQuery()
                    .eq(Ingredient::getFamilyId, familyId)
                    .orderByDesc(Ingredient::getCreateTime)
                    .list();
        } catch (Exception e) {
            log.warn("Query with create_time failed, fallback to simple query: {}", e.getMessage());
            // 降级查询：不使用排序
            return lambdaQuery()
                    .eq(Ingredient::getFamilyId, familyId)
                    .list();
        }
    }

    @Override
    public Ingredient addIngredient(Ingredient ingredient) {
        // 设置默认值
        if (ingredient.getCreateTime() == null) {
            ingredient.setCreateTime(java.time.LocalDateTime.now());
        }
        if (ingredient.getUpdateTime() == null) {
            ingredient.setUpdateTime(java.time.LocalDateTime.now());
        }
        if (ingredient.getIsDeleted() == null) {
            ingredient.setIsDeleted(0);
        }

        // 检查是否即将过期
        if (ingredient.getExpireDate() != null) {
            LocalDate reminderDate = ingredient.getExpireDate().minusDays(
                ingredient.getReminderDays() != null ? ingredient.getReminderDays() : 3
            );
            if (LocalDate.now().isAfter(reminderDate)) {
                ingredient.setStatus(2); // 快过期
            }
        }
        save(ingredient);
        return ingredient;
    }

    @Override
    public Ingredient updateIngredient(Ingredient ingredient) {
        ingredient.setUpdateTime(java.time.LocalDateTime.now());
        updateById(ingredient);
        return ingredient;
    }

    @Override
    public List<Ingredient> recognizeFromImage(String imageBase64) {
        // TODO: 调用百度AI/腾讯云OCR识别食材
        // 模拟返回
        List<Ingredient> list = new ArrayList<>();
        Ingredient i1 = new Ingredient();
        i1.setName("土豆");
        i1.setQuantity(java.math.BigDecimal.valueOf(3));
        i1.setUnit("个");
        list.add(i1);
        return list;
    }

    @Override
    public List<Ingredient> getExpiringIngredients(Long familyId) {
        try {
            LocalDate soon = LocalDate.now().plusDays(3);
            return lambdaQuery()
                    .eq(Ingredient::getFamilyId, familyId)
                    .le(Ingredient::getExpireDate, soon)
                    .ge(Ingredient::getStatus, 1)
                    .list();
        } catch (Exception e) {
            log.warn("Query expiring ingredients failed: {}", e.getMessage());
            return new ArrayList<>();
        }
    }
}
