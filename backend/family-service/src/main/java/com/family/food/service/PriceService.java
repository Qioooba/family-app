package com.family.food.service;

import com.family.food.dto.request.PriceRequest;
import com.family.food.dto.response.PriceResponse;
import com.family.food.dto.response.PriceTrendResponse;

import java.util.List;

public interface PriceService {
    
    List<PriceResponse> comparePrice(PriceRequest request);
    
    PriceTrendResponse getPriceTrend(String barcode, Integer days);
    
    void submitPrice(PriceRequest request);
    
    List<PriceResponse> getPriceAlerts(Long userId);
    
    void setPriceAlert(PriceRequest request);
    
    double calculateDistance(double lat1, double lng1, double lat2, double lng2);
}
