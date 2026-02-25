package com.family.food.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.food.entity.Ingredient;
import com.family.food.mapper.IngredientMapper;
import com.family.food.service.IngredientService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class IngredientServiceImpl extends ServiceImpl<IngredientMapper, Ingredient> implements IngredientService {
    
    @Override
    public List<Ingredient> getFamilyIngredients(Long familyId) {
        return lambdaQuery()
                .eq(Ingredient::getFamilyId, familyId)
                .orderByDesc(Ingredient::getCreateTime)
                .list();
    }
    
    @Override
    public Ingredient addIngredient(Ingredient ingredient) {
        // 检查是否即将过期
        if (ingredient.getExpireDate() != null) {
            LocalDate reminderDate = ingredient.getExpireDate().minusDays(ingredient.getReminderDays());
            if (LocalDate.now().isAfter(reminderDate)) {
                ingredient.setStatus(2); // 快过期
            }
        }
        save(ingredient);
        return ingredient;
    }
    
    @Override
    public Ingredient updateIngredient(Ingredient ingredient) {
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
        LocalDate soon = LocalDate.now().plusDays(3);
        return lambdaQuery()
                .eq(Ingredient::getFamilyId, familyId)
                .le(Ingredient::getExpireDate, soon)
                .ge(Ingredient::getStatus, 1)
                .list();
    }
}
