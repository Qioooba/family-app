package com.family.family.service;

import com.family.family.entity.Inventory;
import com.family.family.entity.ShoppingList;
import com.family.family.vo.PriceTrendVO;

import java.math.BigDecimal;
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
    
    /**
     * 记录价格
     * @param itemName 商品名称
     * @param barcode 条码
     * @param price 价格
     * @param storeName 商店名称
     * @param familyId 家庭ID
     * @return 是否成功
     */
    Boolean recordPrice(String itemName, String barcode, BigDecimal price, String storeName, Long familyId);
    
    /**
     * 获取价格趋势
     * @param itemName 商品名称或条码
     * @return 价格趋势
     */
    PriceTrendVO getPriceTrend(String itemName);
}
