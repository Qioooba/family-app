package com.family.food.service;

import com.family.food.dto.request.ScanRequest;
import com.family.food.dto.response.ScanResponse;

import java.util.List;

/**
 * 扫码识别服务接口
 */
public interface ScanService {
    
    /**
     * 扫码识别商品
     */
    ScanResponse scanProduct(ScanRequest request);
    
    /**
     * 批量扫码识别
     */
    List<ScanResponse> scanBatch(List<ScanRequest> requests);
    
    /**
     * 图片识别食材
     */
    ScanResponse scanIngredient(ScanRequest request);
    
    /**
     * 获取扫码历史记录
     */
    List<ScanResponse> getScanHistory(Long userId, Integer limit);
    
    /**
     * 扫码后添加到库存
     */
    void addToInventory(ScanRequest request);
    
    /**
     * 从条码库查询商品
     */
    ScanResponse queryFromBarcodeLibrary(String barcode);
    
    /**
     * 调用第三方条码API查询
     */
    ScanResponse queryFromThirdPartyApi(String barcode);
}
