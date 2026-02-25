package com.family.family.service.impl;

import com.family.family.entity.Inventory;
import com.family.family.entity.PriceHistory;
import com.family.family.entity.ShoppingList;
import com.family.family.mapper.InventoryMapper;
import com.family.family.mapper.PriceHistoryMapper;
import com.family.family.mapper.ShoppingItemMapper;
import com.family.family.mapper.ShoppingListMapper;
import com.family.family.service.ShoppingService;
import com.family.family.service.BarcodeService;
import com.family.family.vo.PriceTrendVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 购物服务实现
 */
@Service
public class ShoppingServiceImpl implements ShoppingService {
    
    private final ShoppingListMapper shoppingListMapper;
    private final ShoppingItemMapper shoppingItemMapper;
    private final InventoryMapper inventoryMapper;
    private final PriceHistoryMapper priceHistoryMapper;
    private final BarcodeService barcodeService;
    
    public ShoppingServiceImpl(ShoppingListMapper shoppingListMapper, 
                               ShoppingItemMapper shoppingItemMapper,
                               InventoryMapper inventoryMapper,
                               PriceHistoryMapper priceHistoryMapper,
                               BarcodeService barcodeService) {
        this.shoppingListMapper = shoppingListMapper;
        this.shoppingItemMapper = shoppingItemMapper;
        this.inventoryMapper = inventoryMapper;
        this.priceHistoryMapper = priceHistoryMapper;
        this.barcodeService = barcodeService;
    }
    
    @Override
    public Long createList(Long userId, Object request) {
        ShoppingList list = new ShoppingList();
        shoppingListMapper.insert(list);
        return list.getId();
    }
    
    @Override
    public List<ShoppingList> getLists(Long familyId) {
        return shoppingListMapper.selectList(null);
    }
    
    @Override
    public Object getListDetail(Long listId) {
        Map<String, Object> detail = new HashMap<>();
        detail.put("id", listId);
        return detail;
    }
    
    @Override
    public Boolean addItem(Object request) {
        return true;
    }
    
    @Override
    public Boolean updateItemStatus(Long itemId, Integer status, java.math.BigDecimal actualPrice) {
        return true;
    }
    
    @Override
    public Object scanProduct(String barcode) {
        Map<String, Object> result = new HashMap<>();
        result.put("barcode", barcode);
        
        // 查询内置条码库
        BarcodeService.ProductInfo productInfo = barcodeService.lookup(barcode);
        
        if (productInfo != null) {
            result.put("found", true);
            result.put("name", productInfo.getName());
            result.put("category", productInfo.getCategory());
            result.put("brand", productInfo.getBrand());
            
            // 查询历史价格
            List<PriceHistory> history = priceHistoryMapper.selectRecentPrices(productInfo.getName(), barcode);
            if (!history.isEmpty()) {
                BigDecimal avgPrice = history.stream()
                    .map(PriceHistory::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(history.size()), 2, BigDecimal.ROUND_HALF_UP);
                result.put("averagePrice", avgPrice);
                result.put("lastPrice", history.get(0).getPrice());
            }
        } else {
            result.put("found", false);
            result.put("name", "未知商品");
            result.put("category", "其他");
        }
        
        return result;
    }
    
    @Override
    public Long addInventory(Object request) {
        Inventory inventory = new Inventory();
        inventoryMapper.insert(inventory);
        return inventory.getId();
    }
    
    @Override
    public List<Inventory> getInventory(Long familyId) {
        return inventoryMapper.selectList(null);
    }
    
    @Override
    public List<Inventory> getExpiringItems(Long familyId) {
        return new ArrayList<>();
    }
    
    @Override
    public Boolean recordPrice(String itemName, String barcode, BigDecimal price, String storeName, Long familyId) {
        PriceHistory history = new PriceHistory();
        history.setItemName(itemName);
        history.setBarcode(barcode);
        history.setPrice(price);
        history.setStoreName(storeName);
        history.setPurchaseDate(LocalDate.now());
        history.setFamilyId(familyId);
        priceHistoryMapper.insert(history);
        return true;
    }
    
    @Override
    public PriceTrendVO getPriceTrend(String itemName) {
        PriceTrendVO vo = new PriceTrendVO();
        vo.setItemName(itemName);
        
        // 查询该商品的历史价格
        List<PriceHistory> histories = priceHistoryMapper.selectList(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<PriceHistory>()
                .eq(PriceHistory::getItemName, itemName)
                .or()
                .eq(PriceHistory::getBarcode, itemName)
                .orderByDesc(PriceHistory::getPurchaseDate)
        );
        
        if (histories.isEmpty()) {
            return vo;
        }
        
        // 设置条码
        vo.setBarcode(histories.get(0).getBarcode());
        
        // 计算统计数据
        BigDecimal lowest = histories.stream()
            .map(PriceHistory::getPrice)
            .min(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
        BigDecimal highest = histories.stream()
            .map(PriceHistory::getPrice)
            .max(BigDecimal::compareTo)
            .orElse(BigDecimal.ZERO);
        BigDecimal average = histories.stream()
            .map(PriceHistory::getPrice)
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(BigDecimal.valueOf(histories.size()), 2, BigDecimal.ROUND_HALF_UP);
        
        vo.setCurrentPrice(histories.get(0).getPrice());
        vo.setLowestPrice(lowest);
        vo.setHighestPrice(highest);
        vo.setAveragePrice(average);
        
        // 构建价格点列表
        List<PriceTrendVO.PricePoint> points = histories.stream().map(h -> {
            PriceTrendVO.PricePoint point = new PriceTrendVO.PricePoint();
            point.setDate(h.getPurchaseDate());
            point.setPrice(h.getPrice());
            point.setStoreName(h.getStoreName());
            return point;
        }).collect(Collectors.toList());
        
        vo.setPrices(points);
        return vo;
    }
}
