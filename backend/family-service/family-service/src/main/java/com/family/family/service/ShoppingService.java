package com.family.family.service;

import com.family.family.entity.Inventory;
import com.family.family.entity.ShoppingList;

import java.util.List;
import java.util.Map;

/**
 * 购物服务
 */
public interface ShoppingService {
    
    Long createList(Long userId, Object request);
    
    List<ShoppingList> getLists(Long familyId);
    
    Object getListDetail(Long listId);
    
    Boolean addItem(Object request);
    
    Boolean updateItemStatus(Long itemId, Integer status, java.math.BigDecimal actualPrice);
    
    Object scanProduct(String barcode);
    
    Long addInventory(Object request);
    
    List<Inventory> getInventory(Long familyId);
    
    List<Inventory> getExpiringItems(Long familyId);
}
