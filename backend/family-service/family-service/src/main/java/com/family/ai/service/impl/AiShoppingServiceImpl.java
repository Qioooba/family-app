package com.family.ai.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.family.family.entity.AiInventory;
import com.family.family.entity.AiShoppingList;
import com.family.family.entity.AiSubstitute;
import com.family.ai.mapper.AiInventoryMapper;
import com.family.ai.mapper.AiShoppingListMapper;
import com.family.ai.mapper.AiSubstituteMapper;
import com.family.ai.service.AiShoppingService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AI购物助手服务实现
 */
@Service
public class AiShoppingServiceImpl implements AiShoppingService {
    
    private final AiShoppingListMapper shoppingListMapper;
    private final AiInventoryMapper inventoryMapper;
    private final AiSubstituteMapper substituteMapper;
    
    public AiShoppingServiceImpl(AiShoppingListMapper shoppingListMapper,
                                 AiInventoryMapper inventoryMapper,
                                 AiSubstituteMapper substituteMapper) {
        this.shoppingListMapper = shoppingListMapper;
        this.inventoryMapper = inventoryMapper;
        this.substituteMapper = substituteMapper;
    }
    
    @Override
    public AiShoppingList generateShoppingList(Long familyId, Long userId, String occasion, String requirements) {
        // 模拟AI生成购物清单
        AiShoppingList list = new AiShoppingList();
        list.setFamilyId(familyId);
        list.setUserId(userId);
        list.setName(occasion + "购物清单");
        list.setOccasion(occasion);
        
        // 根据场合生成示例物品
        String items = generateItemsByOccasion(occasion, requirements);
        list.setItems(items);
        list.setItemCount(countItems(items));
        list.setCheckedCount(0);
        list.setStatus(1);
        list.setCreateTime(LocalDateTime.now());
        list.setUpdateTime(LocalDateTime.now());
        
        shoppingListMapper.insert(list);
        return list;
    }
    
    @Override
    public AiShoppingList saveShoppingList(AiShoppingList list) {
        list.setItemCount(countItems(list.getItems()));
        list.setUpdateTime(LocalDateTime.now());
        if (list.getId() == null) {
            list.setCheckedCount(0);
            list.setStatus(1);
            list.setCreateTime(LocalDateTime.now());
            shoppingListMapper.insert(list);
        } else {
            shoppingListMapper.updateById(list);
        }
        return list;
    }
    
    @Override
    public void deleteShoppingList(Long listId) {
        AiShoppingList list = shoppingListMapper.selectById(listId);
        if (list != null) {
            list.setStatus(0);
            list.setUpdateTime(LocalDateTime.now());
            shoppingListMapper.updateById(list);
        }
    }
    
    @Override
    public AiShoppingList getShoppingListById(Long listId) {
        return shoppingListMapper.selectById(listId);
    }
    
    @Override
    public List<AiShoppingList> getFamilyShoppingLists(Long familyId) {
        QueryWrapper<AiShoppingList> wrapper = new QueryWrapper<>();
        wrapper.eq("family_id", familyId)
               .eq("status", 1)
               .orderByDesc("create_time");
        return shoppingListMapper.selectList(wrapper);
    }
    
    @Override
    public void checkShoppingItem(Long listId, String itemName, Integer checked) {
        // 简化实现，实际应该解析JSON并更新
        AiShoppingList list = shoppingListMapper.selectById(listId);
        if (list != null) {
            if (checked == 1) {
                list.setCheckedCount(list.getCheckedCount() + 1);
            } else if (list.getCheckedCount() > 0) {
                list.setCheckedCount(list.getCheckedCount() - 1);
            }
            list.setUpdateTime(LocalDateTime.now());
            shoppingListMapper.updateById(list);
        }
    }
    
    @Override
    public AiInventory addInventory(AiInventory inventory) {
        inventory.setStatus(1);
        inventory.setLastUpdated(LocalDateTime.now());
        inventory.setCreateTime(LocalDateTime.now());
        inventory.setUpdateTime(LocalDateTime.now());
        inventoryMapper.insert(inventory);
        return inventory;
    }
    
    @Override
    public void updateInventory(Long inventoryId, BigDecimal quantity) {
        AiInventory inventory = inventoryMapper.selectById(inventoryId);
        if (inventory != null) {
            inventory.setQuantity(quantity);
            inventory.setLastUpdated(LocalDateTime.now());
            inventory.setUpdateTime(LocalDateTime.now());
            inventoryMapper.updateById(inventory);
        }
    }
    
    @Override
    public void deleteInventory(Long inventoryId) {
        AiInventory inventory = inventoryMapper.selectById(inventoryId);
        if (inventory != null) {
            inventory.setStatus(0);
            inventory.setUpdateTime(LocalDateTime.now());
            inventoryMapper.updateById(inventory);
        }
    }
    
    @Override
    public AiInventory getInventoryById(Long inventoryId) {
        return inventoryMapper.selectById(inventoryId);
    }
    
    @Override
    public List<AiInventory> getFamilyInventory(Long familyId) {
        QueryWrapper<AiInventory> wrapper = new QueryWrapper<>();
        wrapper.eq("family_id", familyId)
               .eq("status", 1)
               .orderByDesc("last_updated");
        return inventoryMapper.selectList(wrapper);
    }
    
    @Override
    public List<AiInventory> getLowStockItems(Long familyId) {
        QueryWrapper<AiInventory> wrapper = new QueryWrapper<>();
        wrapper.eq("family_id", familyId)
               .eq("status", 1)
               .eq("alert_enabled", 1)
               .lt("quantity", "min_stock");
        return inventoryMapper.selectList(wrapper);
    }
    
    @Override
    public List<AiInventory> getInventoryByCategory(Long familyId, String category) {
        QueryWrapper<AiInventory> wrapper = new QueryWrapper<>();
        wrapper.eq("family_id", familyId)
               .eq("category", category)
               .eq("status", 1);
        return inventoryMapper.selectList(wrapper);
    }
    
    @Override
    public List<AiSubstitute> getSubstitutes(String ingredient) {
        QueryWrapper<AiSubstitute> wrapper = new QueryWrapper<>();
        wrapper.eq("ingredient", ingredient)
               .eq("status", 1)
               .orderByDesc("usage_count");
        List<AiSubstitute> substitutes = substituteMapper.selectList(wrapper);
        
        // 如果没有找到，返回通用替代建议
        if (substitutes.isEmpty()) {
            substitutes = getDefaultSubstitutes(ingredient);
        }
        
        return substitutes;
    }
    
    @Override
    public AiSubstitute getSubstituteById(Long id) {
        return substituteMapper.selectById(id);
    }
    
    @Override
    public String getSubstituteAdvice(String ingredient, String context) {
        List<AiSubstitute> substitutes = getSubstitutes(ingredient);
        if (substitutes.isEmpty()) {
            return "抱歉，暂时没有找到" + ingredient + "的替代建议。";
        }
        
        AiSubstitute substitute = substitutes.get(0);
        substitute.setUsageCount(substitute.getUsageCount() + 1);
        substitute.setUpdateTime(LocalDateTime.now());
        substituteMapper.updateById(substitute);
        
        return String.format("您可以用%s替代%s，比例为%s。%s",
                substitute.getSubstitute(), ingredient, 
                substitute.getRatio(), substitute.getNotes());
    }
    
    @Override
    public Map<String, Object> optimizePurchase(Long familyId, List<String> items) {
        Map<String, Object> result = new HashMap<>();
        
        // 检查库存，筛选需要购买的物品
        List<String> needToBuy = new ArrayList<>();
        List<String> inStock = new ArrayList<>();
        
        for (String item : items) {
            QueryWrapper<AiInventory> wrapper = new QueryWrapper<>();
            wrapper.eq("family_id", familyId)
                   .like("item_name", item)
                   .eq("status", 1);
            AiInventory inventory = inventoryMapper.selectOne(wrapper);
            
            if (inventory != null && inventory.getQuantity().compareTo(inventory.getMinStock()) > 0) {
                inStock.add(item);
            } else {
                needToBuy.add(item);
            }
        }
        
        result.put("needToBuy", needToBuy);
        result.put("inStock", inStock);
        result.put("savings", inStock.size() + "件物品库存充足，无需购买");
        
        return result;
    }
    
    @Override
    public Map<String, Object> analyzeShoppingHistory(Long familyId, Integer months) {
        Map<String, Object> analysis = new HashMap<>();
        
        // 简化分析
        analysis.put("totalLists", shoppingListMapper.selectCount(
            new QueryWrapper<AiShoppingList>().eq("family_id", familyId).eq("status", 1)));
        analysis.put("avgItemsPerList", 8);
        analysis.put("mostPurchasedCategory", "蔬菜");
        analysis.put("suggestion", "建议每周采购一次，减少临时购物次数");
        
        return analysis;
    }
    
    private String generateItemsByOccasion(String occasion, String requirements) {
        if ("日常".equals(occasion)) {
            return "大米,食用油,盐,鸡蛋,牛奶,苹果,面包";
        } else if ("聚餐".equals(occasion)) {
            return "牛肉,红酒,蔬菜沙拉,蛋糕,饮料,水果拼盘";
        } else if ("火锅".equals(occasion)) {
            return "羊肉卷,肥牛,虾滑,毛肚,豆腐,蔬菜拼盘,火锅底料,蘸料";
        } else if ("烧烤".equals(occasion)) {
            return "羊肉串,鸡翅,玉米,茄子,蘑菇,烧烤料,炭火";
        }
        return "食用油,盐,酱油,醋,鸡蛋,蔬菜";
    }
    
    private int countItems(String items) {
        if (items == null || items.isEmpty()) {
            return 0;
        }
        return items.split(",").length;
    }
    
    private List<AiSubstitute> getDefaultSubstitutes(String ingredient) {
        List<AiSubstitute> defaults = new ArrayList<>();
        
        // 常见食材替代
        if (ingredient.contains("牛奶")) {
            AiSubstitute s = new AiSubstitute();
            s.setIngredient(ingredient);
            s.setSubstitute("豆浆/椰奶");
            s.setRatio("1:1");
            s.setNotes("适合乳糖不耐受人群");
            defaults.add(s);
        } else if (ingredient.contains("鸡蛋")) {
            AiSubstitute s = new AiSubstitute();
            s.setIngredient(ingredient);
            s.setSubstitute("豆腐");
            s.setRatio("1个蛋=50g豆腐");
            s.setNotes("炒鸡蛋可用嫩豆腐替代");
            defaults.add(s);
        } else if (ingredient.contains("糖")) {
            AiSubstitute s = new AiSubstitute();
            s.setIngredient(ingredient);
            s.setSubstitute("蜂蜜");
            s.setRatio("1:0.7");
            s.setNotes("蜂蜜更甜，用量减少");
            defaults.add(s);
        }
        
        return defaults;
    }
}
