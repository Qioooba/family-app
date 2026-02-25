package com.family.food.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.family.family.entity.Ingredient;

import java.util.List;

public interface IngredientService extends IService<Ingredient> {
    
    List<Ingredient> getFamilyIngredients(Long familyId);
    
    Ingredient addIngredient(Ingredient ingredient);
    
    Ingredient updateIngredient(Ingredient ingredient);
    
    List<Ingredient> recognizeFromImage(String imageBase64);
    
    List<Ingredient> getExpiringIngredients(Long familyId);
}
