package com.family.family.service;

import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 简单商品条码库服务
 */
@Service
public class BarcodeService {
    
    // 内置常见商品条码库（可扩展为数据库）
    private static final Map<String, ProductInfo> BARCODE_DB = new HashMap<>();
    
    static {
        // 常见商品数据示例
        BARCODE_DB.put("6902083881405", new ProductInfo("可口可乐330ml", "饮料", "可口可乐"));
        BARCODE_DB.put("6902083880781", new ProductInfo("雪碧330ml", "饮料", "可口可乐"));
        BARCODE_DB.put("6903148047181", new ProductInfo("农夫山泉550ml", "饮料", "农夫山泉"));
        BARCODE_DB.put("6903148034914", new ProductInfo("农夫山泉东方树叶500ml", "饮料", "农夫山泉"));
        BARCODE_DB.put("6925303714128", new ProductInfo("伊利纯牛奶250ml", "乳制品", "伊利"));
        BARCODE_DB.put("6907992512490", new ProductInfo("蒙牛纯牛奶250ml", "乳制品", "蒙牛"));
        BARCODE_DB.put("6903148163034", new ProductInfo("康师傅红烧牛肉面", "方便食品", "康师傅"));
        BARCODE_DB.put("6902083893057", new ProductInfo("奥利奥夹心饼干", "零食", "亿滋"));
        BARCODE_DB.put("6903148042666", new ProductInfo("统一冰红茶500ml", "饮料", "统一"));
        BARCODE_DB.put("6911988006783", new ProductInfo("德芙巧克力", "零食", "玛氏"));
    }
    
    /**
     * 查询商品信息
     * @param barcode 条码
     * @return 商品信息
     */
    public ProductInfo lookup(String barcode) {
        if (barcode == null || barcode.isEmpty()) {
            return null;
        }
        return BARCODE_DB.get(barcode);
    }
    
    /**
     * 商品信息
     */
    public static class ProductInfo {
        private String name;
        private String category;
        private String brand;
        
        public ProductInfo(String name, String category, String brand) {
            this.name = name;
            this.category = category;
            this.brand = brand;
        }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
        
        public String getBrand() { return brand; }
        public void setBrand(String brand) { this.brand = brand; }
    }
}