package com.family.food.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.family.food.dto.request.PriceRequest;
import com.family.food.dto.response.PriceResponse;
import com.family.food.dto.response.PriceTrendResponse;
import com.family.food.entity.PriceAlert;
import com.family.food.entity.PriceRecord;
import com.family.food.entity.Store;
import com.family.food.mapper.PriceAlertMapper;
import com.family.food.mapper.PriceRecordMapper;
import com.family.food.mapper.StoreMapper;
import com.family.food.service.PriceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PriceServiceImpl implements PriceService {
    
    private static final Logger log = LoggerFactory.getLogger(PriceServiceImpl.class);
    
    private final PriceRecordMapper priceRecordMapper;
    private final PriceAlertMapper priceAlertMapper;
    private final StoreMapper storeMapper;
    
    @Autowired
    public PriceServiceImpl(PriceRecordMapper priceRecordMapper, 
                           PriceAlertMapper priceAlertMapper,
                           StoreMapper storeMapper) {
        this.priceRecordMapper = priceRecordMapper;
        this.priceAlertMapper = priceAlertMapper;
        this.storeMapper = storeMapper;
    }
    
    @Override
    public List<PriceResponse> comparePrice(PriceRequest request) {
        String barcode = request.getBarcode();
        String productName = request.getProductName();
        
        QueryWrapper<PriceRecord> wrapper = new QueryWrapper<>();
        if (StrUtil.isNotBlank(barcode)) {
            wrapper.eq("barcode", barcode);
        } else if (StrUtil.isNotBlank(productName)) {
            wrapper.like("product_name", productName);
        }
        wrapper.ge("price_date", LocalDateTime.now().minusDays(7));
        wrapper.orderByDesc("price_date");
        
        List<PriceRecord> records = priceRecordMapper.selectList(wrapper);
        
        Map<Long, PriceRecord> latestPrices = new HashMap<>();
        for (PriceRecord record : records) {
            latestPrices.putIfAbsent(record.getStoreId(), record);
        }
        
        BigDecimal minPrice = latestPrices.values().stream()
                .map(PriceRecord::getPrice)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
        
        List<PriceResponse> responses = new ArrayList<>();
        for (PriceRecord record : latestPrices.values()) {
            PriceResponse response = new PriceResponse();
            response.setStoreId(record.getStoreId());
            response.setStoreName(record.getStoreName());
            response.setPrice(record.getPrice());
            response.setPromotionPrice(record.getPromotionPrice());
            response.setPromotionInfo(record.getPromotionInfo());
            response.setStockStatus(record.getStockStatus());
            response.setUpdateTime(record.getPriceDate());
            response.setIsLowest(record.getPrice().compareTo(minPrice) == 0);
            response.setPriceDiff(record.getPrice().subtract(minPrice));
            responses.add(response);
        }
        
        String sortBy = request.getSortBy();
        if ("price_asc".equals(sortBy)) {
            responses.sort(Comparator.comparing(PriceResponse::getPrice));
        } else if ("price_desc".equals(sortBy)) {
            responses.sort(Comparator.comparing(PriceResponse::getPrice).reversed());
        }
        
        return responses;
    }
    
    @Override
    public PriceTrendResponse getPriceTrend(String barcode, Integer days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<PriceRecord> records = priceRecordMapper.selectPriceHistory(barcode, startDate);
        
        PriceTrendResponse response = new PriceTrendResponse();
        response.setBarcode(barcode);
        
        if (records.isEmpty()) {
            response.setTrend("stable");
            return response;
        }
        
        response.setProductName(records.get(0).getProductName());
        
        Map<LocalDate, List<PriceRecord>> dailyGroups = records.stream()
                .collect(Collectors.groupingBy(r -> r.getPriceDate().toLocalDate()));
        
        List<PriceTrendResponse.PriceHistoryPoint> points = new ArrayList<>();
        BigDecimal minPrice = BigDecimal.valueOf(Double.MAX_VALUE);
        BigDecimal maxPrice = BigDecimal.ZERO;
        BigDecimal totalPrice = BigDecimal.ZERO;
        int count = 0;
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        for (Map.Entry<LocalDate, List<PriceRecord>> entry : dailyGroups.entrySet()) {
            List<PriceRecord> dayRecords = entry.getValue();
            
            BigDecimal dayAvg = dayRecords.stream()
                    .map(PriceRecord::getPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(dayRecords.size()), 2, RoundingMode.HALF_UP);
            
            BigDecimal dayMin = dayRecords.stream()
                    .map(PriceRecord::getPrice)
                    .min(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);
            
            BigDecimal dayMax = dayRecords.stream()
                    .map(PriceRecord::getPrice)
                    .max(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);
            
            PriceTrendResponse.PriceHistoryPoint point = new PriceTrendResponse.PriceHistoryPoint();
            point.setDate(entry.getKey().format(formatter));
            point.setAvgPrice(dayAvg);
            point.setMinPrice(dayMin);
            point.setMaxPrice(dayMax);
            points.add(point);
            
            minPrice = minPrice.min(dayMin);
            maxPrice = maxPrice.max(dayMax);
            totalPrice = totalPrice.add(dayAvg);
            count++;
        }
        
        points.sort(Comparator.comparing(PriceTrendResponse.PriceHistoryPoint::getDate));
        response.setHistoryPoints(points);
        response.setMinPrice(minPrice);
        response.setMaxPrice(maxPrice);
        
        if (count > 0) {
            response.setCurrentAvgPrice(totalPrice.divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP));
        }
        
        if (points.size() >= 2) {
            PriceTrendResponse.PriceHistoryPoint first = points.get(0);
            PriceTrendResponse.PriceHistoryPoint last = points.get(points.size() - 1);
            
            if (first.getAvgPrice().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal diff = last.getAvgPrice().subtract(first.getAvgPrice());
                BigDecimal percent = diff.multiply(BigDecimal.valueOf(100))
                        .divide(first.getAvgPrice(), 2, RoundingMode.HALF_UP);
                
                response.setTrendPercent(percent);
                if (percent.compareTo(BigDecimal.valueOf(5)) > 0) {
                    response.setTrend("up");
                } else if (percent.compareTo(BigDecimal.valueOf(-5)) < 0) {
                    response.setTrend("down");
                } else {
                    response.setTrend("stable");
                }
            }
        } else {
            response.setTrend("stable");
            response.setTrendPercent(BigDecimal.ZERO);
        }
        
        return response;
    }
    
    @Override
    public void submitPrice(PriceRequest request) {
        PriceRecord record = new PriceRecord();
        record.setBarcode(request.getBarcode());
        record.setProductName(request.getProductName());
        record.setBrand(request.getBrand());
        record.setSpecification(request.getSpecification());
        record.setStoreName(request.getStoreName());
        record.setPrice(request.getCurrentPrice());
        record.setPriceDate(LocalDateTime.now());
        record.setVerifyStatus(0);
        record.setSource(1);
        
        priceRecordMapper.insert(record);
        
        checkPriceAlerts(request.getBarcode(), request.getCurrentPrice());
    }
    
    @Override
    public List<PriceResponse> getPriceAlerts(Long userId) {
        List<PriceAlert> alerts = priceAlertMapper.selectActiveAlertsByUserId(userId);
        
        List<PriceResponse> responses = new ArrayList<>();
        for (PriceAlert alert : alerts) {
            QueryWrapper<PriceRecord> wrapper = new QueryWrapper<>();
            wrapper.eq("barcode", alert.getBarcode());
            wrapper.orderByDesc("price_date");
            wrapper.last("LIMIT 1");
            
            PriceRecord record = priceRecordMapper.selectOne(wrapper);
            if (record != null) {
                PriceResponse response = new PriceResponse();
                response.setPrice(record.getPrice());
                response.setPromotionPrice(record.getPromotionPrice());
                response.setStoreName(record.getStoreName());
                responses.add(response);
            }
        }
        
        return responses;
    }
    
    @Override
    public void setPriceAlert(PriceRequest request) {
        PriceAlert alert = new PriceAlert();
        alert.setBarcode(request.getBarcode());
        alert.setProductName(request.getProductName());
        alert.setTargetPrice(request.getCurrentPrice());
        alert.setAlertType("below");
        alert.setStatus(1);
        
        priceAlertMapper.insert(alert);
    }
    
    @Override
    public double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        final int R = 6371;
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lngDistance = Math.toRadians(lng2 - lng1);
        
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }
    
    private void checkPriceAlerts(String barcode, BigDecimal currentPrice) {
        QueryWrapper<PriceAlert> wrapper = new QueryWrapper<>();
        wrapper.eq("barcode", barcode);
        wrapper.eq("status", 1);
        
        List<PriceAlert> alerts = priceAlertMapper.selectList(wrapper);
        
        for (PriceAlert alert : alerts) {
            if (currentPrice.compareTo(alert.getTargetPrice()) <= 0) {
                log.info("价格提醒触发: userId={}, barcode={}, targetPrice={}, currentPrice={}",
                        alert.getUserId(), barcode, alert.getTargetPrice(), currentPrice);
            }
        }
    }
}
