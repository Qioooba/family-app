package com.family.family.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.family.family.controller.ShoppingController;
import com.family.family.entity.Inventory;
import com.family.family.entity.ShoppingItem;
import com.family.family.entity.ShoppingList;
import com.family.family.mapper.InventoryMapper;
import com.family.family.mapper.ShoppingItemMapper;
import com.family.family.mapper.ShoppingListMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 智能购物服务
 */
@Service
public class ShoppingService {
    
    private final ShoppingListMapper shoppingListMapper;
    private final ShoppingItemMapper shoppingItemMapper;
    private final InventoryMapper inventoryMapper;
    
    public ShoppingService(ShoppingListMapper shoppingListMapper, ShoppingItemMapper shoppingItemMapper, InventoryMapper inventoryMapper) {
        this.shoppingListMapper = shoppingListMapper;
        this.shoppingItemMapper = shoppingItemMapper;
        this.inventoryMapper = inventoryMapper;
    }
    
    @Transactional
    public Long createList(Long userId, ShoppingController.CreateListRequest request) {
        ShoppingList list = new ShoppingList();
        list.setFamilyId(request.getFamilyId());
        list.setName(request.getName());
        list.setType(request.getType());
        list.setStatus(0);
        list.setTotalAmount(BigDecimal.ZERO);
        list.setCreatorId(userId);
        list.setCreateTime(LocalDateTime.now());
        shoppingListMapper.insert(list);
        return list.getId();
    }
    
    public List<ShoppingList> getLists(Long familyId) {
        LambdaQueryWrapper<ShoppingList> wrapper = new LambdaQueryWrapper<ShoppingList>()
                .eq(ShoppingList::getFamilyId, familyId)
                .eq(ShoppingList::getStatus, 0)
                .orderByDesc(ShoppingList::getCreateTime);
        return shoppingListMapper.selectList(wrapper);
    }
    
    public Map<String, Object> getListDetail(Long listId) {
        ShoppingList list = shoppingListMapper.selectById(listId);
        LambdaQueryWrapper<ShoppingItem> wrapper = new LambdaQueryWrapper<ShoppingItem>()
                .eq(ShoppingItem::getListId, listId)
                .orderByDesc(ShoppingItem::getCreateTime);
        List<ShoppingItem> items = shoppingItemMapper.selectList(wrapper);
        
        Map<String, Object> result = new HashMap<>();
        result.put("list", list);
        result.put("items", items);
        return result;
    }
    
    public Boolean addItem(ShoppingController.AddItemRequest request) {
        ShoppingItem item = new ShoppingItem();
        item.setListId(request.getListId());
        item.setName(request.getName());
        item.setCategory(request.getCategory());
        item.setQuantity(request.getQuantity());
        item.setUnit(request.getUnit());
        item.setEstimatedPrice(request.getEstimatedPrice());
        item.setBarcode(request.getBarcode());
        item.setAssigneeId(request.getAssigneeId());
        item.setStatus(0);
        item.setCreateTime(LocalDateTime.now());
        shoppingItemMapper.insert(item);
        return true;
    }
    
    public Boolean updateItemStatus(Long itemId, Integer status, BigDecimal actualPrice) {
        ShoppingItem item = shoppingItemMapper.selectById(itemId);
        if (item == null) return false;
        
        item.setStatus(status);
        if (actualPrice != null) {
            item.setActualPrice(actualPrice);
        }
        shoppingItemMapper.updateById(item);
        
        if (status == 1 && actualPrice != null) {
            updateListTotal(item.getListId());
        }
        return true;
    }
    
    private void updateListTotal(Long listId) {
        LambdaQueryWrapper<ShoppingItem> wrapper = new LambdaQueryWrapper<ShoppingItem>()
                .eq(ShoppingItem::getListId, listId)
                .eq(ShoppingItem::getStatus, 1);
        List<ShoppingItem> items = shoppingItemMapper.selectList(wrapper);
        
        BigDecimal total = BigDecimal.ZERO;
        for (ShoppingItem item : items) {
            if (item.getActualPrice() != null) {
                total = total.add(item.getActualPrice());
            }
        }
        
        ShoppingList list = shoppingListMapper.selectById(listId);
        list.setTotalAmount(total);
        shoppingListMapper.updateById(list);
    }
    
    public Map<String, Object> scanProduct(String barcode) {
        Map<String, Object> product = new HashMap<>();
        product.put("barcode", barcode);
        product.put("name", "扫描商品示例");
        product.put("brand", "示例品牌");
        product.put("spec", "500g");
        product.put("price", 19.9);
        return product;
    }
    
    public Long addInventory(ShoppingController.AddInventoryRequest request) {
        Inventory inventory = new Inventory();
        inventory.setFamilyId(request.getFamilyId());
        inventory.setName(request.getName());
        inventory.setCategory(request.getCategory());
        inventory.setQuantity(request.getQuantity());
        inventory.setUnit(request.getUnit());
        inventory.setStorageLocation(request.getStorageLocation());
        inventory.setExpireDate(LocalDate.parse(request.getExpireDate()));
        inventory.setBarcode(request.getBarcode());
        inventory.setPrice(request.getPrice());
        inventory.setCreateTime(LocalDateTime.now());
        inventoryMapper.insert(inventory);
        return inventory.getId();
    }
    
    public List<Inventory> getInventory(Long familyId) {
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getFamilyId, familyId)
                .orderByDesc(Inventory::getCreateTime);
        return inventoryMapper.selectList(wrapper);
    }
    
    public List<Inventory> getExpiringItems(Long familyId) {
        LocalDate threshold = LocalDate.now().plusDays(7);
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<Inventory>()
                .eq(Inventory::getFamilyId, familyId)
                .le(Inventory::getExpireDate, threshold)
                .orderByAsc(Inventory::getExpireDate);
        return inventoryMapper.selectList(wrapper);
    }
}
