package com.family.food.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.family.food.entity.PriceRecord;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface PriceRecordMapper extends BaseMapper<PriceRecord> {
    
    @Select("SELECT id, barcode, product_name, brand, specification, store_id, store_name, price, promotion_price, promotion_info, stock_status, price_date, latitude, longitude, submitter_id, verify_status, source, create_time, update_time, status FROM price_record WHERE barcode = #{barcode} AND price_date >= #{startDate} ORDER BY price_date")
    List<PriceRecord> selectPriceHistory(@Param("barcode") String barcode, @Param("startDate") LocalDateTime startDate);
    
    @Select("SELECT id, barcode, product_name, brand, specification, store_id, store_name, price, promotion_price, promotion_info, stock_status, price_date, latitude, longitude, submitter_id, verify_status, source, create_time, update_time, status FROM price_record WHERE barcode = #{barcode} AND store_id = #{storeId} ORDER BY price_date DESC LIMIT 1")
    PriceRecord selectLatestByBarcodeAndStore(@Param("barcode") String barcode, @Param("storeId") Long storeId);
}
