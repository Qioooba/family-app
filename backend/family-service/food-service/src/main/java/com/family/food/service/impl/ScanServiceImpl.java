package com.family.food.service.impl;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.family.food.dto.request.ScanRequest;
import com.family.food.dto.response.ScanResponse;
import com.family.food.entity.BarcodeProduct;
import com.family.food.entity.Ingredient;
import com.family.food.entity.ScanRecord;
import com.family.food.mapper.BarcodeProductMapper;
import com.family.food.mapper.ScanRecordMapper;
import com.family.food.service.IngredientService;
import com.family.food.service.ScanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 扫码识别服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ScanServiceImpl extends ServiceImpl<ScanRecordMapper, ScanRecord> implements ScanService {
    
    private final BarcodeProductMapper barcodeProductMapper;
    private final IngredientService ingredientService;
    private final StringRedisTemplate redisTemplate;
    
    // 条码查询API配置
    private static final String BARCODE_API_URL = "https://api.qrserver.com/v1/read-qr-code/";
    private static final String UPC_API_URL = "https://api.upcitemdb.com/prod/trial/lookup";
    
    // 条形码正则
    private static final Pattern BARCODE_PATTERN = Pattern.compile("^[0-9]{8,14}$");
    
    @Override
    public ScanResponse scanProduct(ScanRequest request) {
        String scanContent = request.getImageBase64();
        if (StrUtil.isBlank(scanContent)) {
            return buildErrorResponse("扫码内容不能为空");
        }
        
        ScanResponse response;
        
        // 判断是条形码还是二维码
        if (isBarcode(scanContent)) {
            // 先从本地库查询
            response = queryFromBarcodeLibrary(scanContent);
            if (response == null) {
                // 本地库没有，调用第三方API
                response = queryFromThirdPartyApi(scanContent);
            }
        } else {
            // 二维码处理
            response = handleQRCode(scanContent);
        }
        
        // 保存扫码记录
        saveScanRecord(request, response);
        
        return response;
    }
    
    @Override
    public List<ScanResponse> scanBatch(List<ScanRequest> requests) {
        List<ScanResponse> responses = new ArrayList<>();
        for (ScanRequest request : requests) {
            try {
                ScanResponse response = scanProduct(request);
                responses.add(response);
            } catch (Exception e) {
                log.error("批量扫码失败: {}", request.getImageBase64(), e);
                responses.add(buildErrorResponse("识别失败: " + e.getMessage()));
            }
        }
        return responses;
    }
    
    @Override
    public ScanResponse scanIngredient(ScanRequest request) {
        String imageBase64 = request.getImageBase64();
        if (StrUtil.isBlank(imageBase64)) {
            return buildErrorResponse("图片不能为空");
        }
        
        // 调用AI服务识别图片中的食材
        List<Ingredient> ingredients = ingredientService.recognizeFromImage(imageBase64);
        
        ScanResponse response = new ScanResponse();
        if (ingredients.isEmpty()) {
            response.setStatus("fail");
            response.setMessage("未能识别出食材");
        } else {
            response.setStatus("success");
            response.setProductName(ingredients.get(0).getName());
            response.setCategory(ingredients.get(0).getCategory());
            response.setRawData(JSONUtil.toJsonStr(ingredients));
            response.setMessage("识别成功，共识别出" + ingredients.size() + "种食材");
        }
        
        // 保存扫码记录
        saveScanRecord(request, response);
        
        return response;
    }
    
    @Override
    public List<ScanResponse> getScanHistory(Long userId, Integer limit) {
        List<ScanRecord> records = lambdaQuery()
                .eq(ScanRecord::getUserId, userId)
                .orderByDesc(ScanRecord::getCreateTime)
                .last("LIMIT " + limit)
                .list();
        
        List<ScanResponse> responses = new ArrayList<>();
        for (ScanRecord record : records) {
            ScanResponse response = new ScanResponse();
            response.setStatus(record.getStatus() == 1 ? "success" : "fail");
            response.setProductName(record.getProductName());
            response.setMessage(record.getFailReason());
            if (StrUtil.isNotBlank(record.getResultJson())) {
                try {
                    ScanResponse parsed = JSONUtil.toBean(record.getResultJson(), ScanResponse.class);
                    response.setBarcode(parsed.getBarcode());
                    response.setBrand(parsed.getBrand());
                    response.setSpecification(parsed.getSpecification());
                    response.setCategory(parsed.getCategory());
                    response.setReferencePrice(parsed.getReferencePrice());
                    response.setRawData(record.getResultJson());
                } catch (Exception e) {
                    log.warn("解析历史记录失败: {}", record.getId());
                }
            }
            responses.add(response);
        }
        return responses;
    }
    
    @Override
    public void addToInventory(ScanRequest request) {
        // 先扫码识别
        ScanResponse response = scanProduct(request);
        
        if (!"success".equals(response.getStatus())) {
            throw new RuntimeException("扫码识别失败，无法添加到库存");
        }
        
        // 创建食材实体
        Ingredient ingredient = new Ingredient();
        ingredient.setFamilyId(request.getFamilyId());
        ingredient.setName(response.getProductName());
        ingredient.setCategory(response.getCategory());
        ingredient.setQuantity(BigDecimal.ONE);
        ingredient.setUnit("件");
        ingredient.setPurchaseDate(LocalDate.now());
        
        // 设置过期日期
        if (response.getShelfLifeDays() != null) {
            ingredient.setExpireDate(LocalDate.now().plusDays(response.getShelfLifeDays()));
        }
        
        ingredient.setReminderDays(3);
        ingredient.setStatus(1);
        ingredient.setRecognizedData(JSONUtil.toJsonStr(response));
        
        // 保存到库存
        ingredientService.addIngredient(ingredient);
        
        // 更新扫码记录为已添加
        lambdaUpdate()
                .eq(ScanRecord::getUserId, request.getUserId())
                .eq(ScanRecord::getScanContent, request.getImageBase64())
                .set(ScanRecord::getAddedToInventory, true)
                .update();
    }
    
    @Override
    public ScanResponse queryFromBarcodeLibrary(String barcode) {
        // 先从Redis缓存查询
        String cacheKey = "barcode:" + barcode;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (StrUtil.isNotBlank(cached)) {
            return JSONUtil.toBean(cached, ScanResponse.class);
        }
        
        // 查询本地库
        BarcodeProduct product = barcodeProductMapper.selectOne(
                barcodeProductMapper.lambdaQuery()
                        .eq(BarcodeProduct::getBarcode, barcode)
                        .getWrapper()
        );
        
        if (product == null) {
            return null;
        }
        
        // 增加查询次数
        barcodeProductMapper.incrementQueryCount(product.getId());
        
        ScanResponse response = buildResponseFromProduct(product);
        
        // 缓存结果
        redisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(response), 7, TimeUnit.DAYS);
        
        return response;
    }
    
    @Override
    public ScanResponse queryFromThirdPartyApi(String barcode) {
        try {
            // 调用UPC API
            String url = UPC_API_URL + "?upc=" + barcode;
            String result = HttpUtil.get(url, 5000);
            
            JSONObject json = JSONUtil.parseObj(result);
            JSONArray items = json.getJSONArray("items");
            
            if (items == null || items.isEmpty()) {
                return buildErrorResponse("未找到该商品信息");
            }
            
            JSONObject item = items.getJSONObject(0);
            ScanResponse response = new ScanResponse();
            response.setStatus("success");
            response.setBarcode(barcode);
            response.setProductName(item.getStr("title", "未知商品"));
            response.setBrand(item.getStr("brand", ""));
            response.setRawData(result);
            
            // 提取类别
            JSONArray offers = item.getJSONArray("offers");
            if (offers != null && !offers.isEmpty()) {
                JSONObject offer = offers.getJSONObject(0);
                response.setReferencePrice(new BigDecimal(offer.getStr("price", "0")));
            }
            
            // 保存到本地库(异步)
            saveToBarcodeLibrary(barcode, response);
            
            // 缓存结果
            String cacheKey = "barcode:" + barcode;
            redisTemplate.opsForValue().set(cacheKey, JSONUtil.toJsonStr(response), 7, TimeUnit.DAYS);
            
            return response;
            
        } catch (Exception e) {
            log.error("第三方API查询失败: {} - {}", barcode, e.getMessage());
            return buildErrorResponse("商品信息查询失败");
        }
    }
    
    /**
     * 处理二维码
     */
    private ScanResponse handleQRCode(String content) {
        ScanResponse response = new ScanResponse();
        response.setStatus("success");
        response.setBarcode(content);
        response.setProductName("二维码内容");
        response.setRawData(content);
        response.setMessage("二维码识别成功");
        return response;
    }
    
    /**
     * 判断是否为条形码
     */
    private boolean isBarcode(String content) {
        if (StrUtil.isBlank(content)) {
            return false;
        }
        Matcher matcher = BARCODE_PATTERN.matcher(content);
        return matcher.matches();
    }
    
    /**
     * 从商品实体构建响应
     */
    private ScanResponse buildResponseFromProduct(BarcodeProduct product) {
        ScanResponse response = new ScanResponse();
        response.setStatus("success");
        response.setBarcode(product.getBarcode());
        response.setProductName(product.getProductName());
        response.setBrand(product.getBrand());
        response.setSpecification(product.getSpecification());
        response.setCategory(product.getCategory());
        response.setReferencePrice(product.getReferencePrice());
        response.setShelfLifeDays(product.getShelfLifeDays());
        if (product.getShelfLifeDays() != null) {
            response.setExpireDate(LocalDate.now().plusDays(product.getShelfLifeDays()));
        }
        response.setMessage("识别成功");
        return response;
    }
    
    /**
     * 构建错误响应
     */
    private ScanResponse buildErrorResponse(String message) {
        ScanResponse response = new ScanResponse();
        response.setStatus("fail");
        response.setMessage(message);
        return response;
    }
    
    /**
     * 保存扫码记录
     */
    private void saveScanRecord(ScanRequest request, ScanResponse response) {
        try {
            ScanRecord record = new ScanRecord();
            record.setUserId(request.getUserId());
            record.setFamilyId(request.getFamilyId());
            record.setScanType(request.getScanType());
            record.setScanContent(request.getImageBase64());
            record.setStatus("success".equals(response.getStatus()) ? 1 : 0);
            record.setProductName(response.getProductName());
            record.setFailReason(response.getMessage());
            record.setResultJson(JSONUtil.toJsonStr(response));
            record.setAddedToInventory(false);
            save(record);
        } catch (Exception e) {
            log.error("保存扫码记录失败", e);
        }
    }
    
    /**
     * 保存到本地条码库
     */
    private void saveToBarcodeLibrary(String barcode, ScanResponse response) {
        try {
            BarcodeProduct product = new BarcodeProduct();
            product.setBarcode(barcode);
            product.setProductName(response.getProductName());
            product.setBrand(response.getBrand());
            product.setCategory(response.getCategory());
            product.setReferencePrice(response.getReferencePrice());
            product.setSource("api");
            product.setQueryCount(1);
            barcodeProductMapper.insert(product);
        } catch (Exception e) {
            log.warn("保存到条码库失败(可能已存在): {} - {}", barcode, e.getMessage());
        }
    }
}
