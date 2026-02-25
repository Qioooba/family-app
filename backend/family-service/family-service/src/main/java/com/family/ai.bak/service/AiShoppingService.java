package com.family.ai.service;

import com.family.ai.entity.AiInventory;
import com.family.ai.entity.AiShoppingList;
import com.family.ai.entity.AiSubstitute;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * AI购物助手服务
 */
public interface AiShoppingService {
    
    // 购物清单
    AiShoppingList generateShoppingList(Long familyId, Long userId, String occasion, String requirements);
    AiShoppingList saveShoppingList(AiShoppingList list);
    void deleteShoppingList(Long listId);
    AiShoppingList getShoppingListById(Long listId);
    List<AiShoppingList> getFamilyShoppingLists(Long familyId);
    void checkShoppingItem(Long listId, String itemName, Integer checked);
    
    // 库存管理
    AiInventory addInventory(AiInventory inventory);
    void updateInventory(Long inventoryId, BigDecimal quantity);
    void deleteInventory(Long inventoryId);
    AiInventory getInventoryById(Long inventoryId);
    List<AiInventory> getFamilyInventory(Long familyId);
    List<AiInventory> getLowStockItems(Long familyId);
    List<AiInventory> getInventoryByCategory(Long familyId, String category);
    
    // 食材替代
    List<AiSubstitute> getSubstitutes(String ingredient);
    AiSubstitute getSubstituteById(Long id);
    String getSubstituteAdvice(String ingredient, String context);
    
    // 采购优化
    Map<String, Object> optimizePurchase(Long familyId, List<String> items);
    Map<String, Object> analyzeShoppingHistory(Long familyId, Integer months);
}
