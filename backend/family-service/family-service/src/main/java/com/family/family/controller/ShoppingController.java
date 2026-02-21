package com.family.family.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.family.common.core.Result;
import com.family.family.entity.Inventory;
import com.family.family.entity.ShoppingList;
import com.family.family.service.ShoppingService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 智能购物控制器
 */
@RestController
@RequestMapping("/api/shopping")
public class ShoppingController {
    
    private final ShoppingService shoppingService;
    
    public ShoppingController(ShoppingService shoppingService) {
        this.shoppingService = shoppingService;
    }
    
    @PostMapping("/list/create")
    public Result<Long> createList(@RequestBody CreateListRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.success(shoppingService.createList(userId, request));
    }
    
    @GetMapping("/list/{familyId}")
    public Result<List<ShoppingList>> getLists(@PathVariable Long familyId) {
        return Result.success(shoppingService.getLists(familyId));
    }
    
    @GetMapping("/list/detail/{listId}")
    public Result<Object> getListDetail(@PathVariable Long listId) {
        return Result.success(shoppingService.getListDetail(listId));
    }
    
    @PostMapping("/item/add")
    public Result<Boolean> addItem(@RequestBody AddItemRequest request) {
        return Result.success(shoppingService.addItem(request));
    }
    
    @PutMapping("/item/{itemId}/status")
    public Result<Boolean> updateItemStatus(
            @PathVariable Long itemId,
            @RequestParam Integer status,
            @RequestParam(required = false) BigDecimal actualPrice) {
        return Result.success(shoppingService.updateItemStatus(itemId, status, actualPrice));
    }
    
    @PostMapping("/scan")
    public Result<Object> scanProduct(@RequestBody ScanRequest request) {
        return Result.success(shoppingService.scanProduct(request.getBarcode()));
    }
    
    @PostMapping("/inventory/add")
    public Result<Long> addInventory(@RequestBody AddInventoryRequest request) {
        return Result.success(shoppingService.addInventory(request));
    }
    
    @GetMapping("/inventory/{familyId}")
    public Result<List<Inventory>> getInventory(@PathVariable Long familyId) {
        return Result.success(shoppingService.getInventory(familyId));
    }
    
    @GetMapping("/inventory/expiring/{familyId}")
    public Result<List<Inventory>> getExpiringItems(@PathVariable Long familyId) {
        return Result.success(shoppingService.getExpiringItems(familyId));
    }
    
    public static class CreateListRequest {
        private Long familyId;
        private String name;
        private String type;
        
        public Long getFamilyId() {
            return familyId;
        }
        
        public void setFamilyId(Long familyId) {
            this.familyId = familyId;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getType() {
            return type;
        }
        
        public void setType(String type) {
            this.type = type;
        }
    }
    
    public static class AddItemRequest {
        private Long listId;
        private String name;
        private String category;
        private BigDecimal quantity;
        private String unit;
        private BigDecimal estimatedPrice;
        private String barcode;
        private Long assigneeId;
        
        public Long getListId() {
            return listId;
        }
        
        public void setListId(Long listId) {
            this.listId = listId;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getCategory() {
            return category;
        }
        
        public void setCategory(String category) {
            this.category = category;
        }
        
        public BigDecimal getQuantity() {
            return quantity;
        }
        
        public void setQuantity(BigDecimal quantity) {
            this.quantity = quantity;
        }
        
        public String getUnit() {
            return unit;
        }
        
        public void setUnit(String unit) {
            this.unit = unit;
        }
        
        public BigDecimal getEstimatedPrice() {
            return estimatedPrice;
        }
        
        public void setEstimatedPrice(BigDecimal estimatedPrice) {
            this.estimatedPrice = estimatedPrice;
        }
        
        public String getBarcode() {
            return barcode;
        }
        
        public void setBarcode(String barcode) {
            this.barcode = barcode;
        }
        
        public Long getAssigneeId() {
            return assigneeId;
        }
        
        public void setAssigneeId(Long assigneeId) {
            this.assigneeId = assigneeId;
        }
    }
    
    public static class ScanRequest {
        private String barcode;
        private String image;
        
        public String getBarcode() {
            return barcode;
        }
        
        public void setBarcode(String barcode) {
            this.barcode = barcode;
        }
        
        public String getImage() {
            return image;
        }
        
        public void setImage(String image) {
            this.image = image;
        }
    }
    
    public static class AddInventoryRequest {
        private Long familyId;
        private String name;
        private String category;
        private BigDecimal quantity;
        private String unit;
        private String storageLocation;
        private String expireDate;
        private String barcode;
        private BigDecimal price;
        
        public Long getFamilyId() {
            return familyId;
        }
        
        public void setFamilyId(Long familyId) {
            this.familyId = familyId;
        }
        
        public String getName() {
            return name;
        }
        
        public void setName(String name) {
            this.name = name;
        }
        
        public String getCategory() {
            return category;
        }
        
        public void setCategory(String category) {
            this.category = category;
        }
        
        public BigDecimal getQuantity() {
            return quantity;
        }
        
        public void setQuantity(BigDecimal quantity) {
            this.quantity = quantity;
        }
        
        public String getUnit() {
            return unit;
        }
        
        public void setUnit(String unit) {
            this.unit = unit;
        }
        
        public String getStorageLocation() {
            return storageLocation;
        }
        
        public void setStorageLocation(String storageLocation) {
            this.storageLocation = storageLocation;
        }
        
        public String getExpireDate() {
            return expireDate;
        }
        
        public void setExpireDate(String expireDate) {
            this.expireDate = expireDate;
        }
        
        public String getBarcode() {
            return barcode;
        }
        
        public void setBarcode(String barcode) {
            this.barcode = barcode;
        }
        
        public BigDecimal getPrice() {
            return price;
        }
        
        public void setPrice(BigDecimal price) {
            this.price = price;
        }
    }
}
